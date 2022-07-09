package com.example.gateway.manage.service;

import com.example.client.proto.Echo;
import com.example.gateway.ManagementProto;
import com.example.gateway.MessageGrpc;
import com.example.gateway.server.message.BaseMessage;
import com.example.gateway.server.message.MessageFactory;
import com.example.gateway.server.session.NetSession;
import com.example.gateway.server.session.SessionManager;
import com.example.gateway.service.auth.AuthInfo;
import com.example.gateway.service.auth.JwtService;
import com.google.protobuf.MessageLite;
import io.grpc.stub.StreamObserver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;

@Component
@Slf4j
public class MessageImpl extends MessageGrpc.MessageImplBase {
    private final SessionManager sessionManager;
    private final JwtService JWTService;

    public MessageImpl(SessionManager sessionManager, JwtService JWTService) {
        this.sessionManager = sessionManager;
        this.JWTService = JWTService;
    }

    @Override
    public void sendMessage(ManagementProto.MessageRequest request, StreamObserver<ManagementProto.Response> responseObserver) {
        AuthInfo authInfo = this.JWTService.fromJwt(request.getJwt());
        if (!authInfo.isCredentialsNonExpired()) {
            this.sendResponse(responseObserver, 101, "token expired!");
            return;
        }

        Optional<NetSession> sessionOptional = this.sessionManager.get(authInfo.getUserId());
        if (sessionOptional.isEmpty()) {
            this.sendResponse(responseObserver, 101, "user not online!");
            return;
        }

        BaseMessage message = MessageFactory.make((short) request.getCommandId());
        if (request.getType() == ManagementProto.MessageType.Text) {
            message.setPayload(this.buildMessageContent(request.getPayload()));
        } else {
            message.setPayload(this.decodeContent(request.getPayload()));
        }

        this.sendResponse(responseObserver, 0, "done");
    }


    private ByteBuf decodeContent(String base64Str) {
        byte[] bytes = Base64.getDecoder().decode(base64Str);
        ByteBuf byteBuf = Unpooled.buffer(bytes.length);
        byteBuf.writeBytes(bytes);

        return byteBuf;
    }

    private MessageLite buildMessageContent(String content) {
        Echo.EchoResponse.Builder builder = Echo.EchoResponse.newBuilder();
        builder.setTs(0);
        builder.setContent(content);
        return builder.build();
    }

    private void sendResponse(StreamObserver<ManagementProto.Response> responseObserver, int code, String message) {
        ManagementProto.Response response = ManagementProto.Response.newBuilder()
                .setCode(code)
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

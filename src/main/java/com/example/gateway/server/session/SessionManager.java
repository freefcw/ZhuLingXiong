package com.example.gateway.server.session;

import com.example.gateway.server.message.BaseMessage;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class SessionManager implements ApplicationContextAware {
    private final static ConcurrentHashMap<Integer, NetSession> USER_SESSION = new ConcurrentHashMap<>();
    private ApplicationContext appContext;

    public SessionManager() {
    }

    public Stat getStatInfo() {
        Stat stat = new Stat();
        stat.total = USER_SESSION.size();

        return stat;
    }

    public Optional<NetSession> get(Integer userId) {
        if (USER_SESSION.containsKey(userId)) {
            return Optional.of(USER_SESSION.get(userId));
        }

        return Optional.empty();
    }

    public List<Integer> getUsers() {
        List<Integer> users = new ArrayList<>();
        USER_SESSION.keys().asIterator().forEachRemaining(users::add);

        return users;
    }

    @Data
    public static class Stat {
        private Integer total;
    }

    public NetSession newSession(Channel channel) {
        NetSession netSession = new NetSession(channel);
        channel.attr(NetSession.sessionKey()).set(netSession);

        return netSession;
    }

    public void inactive(Channel channel) {
        if (!channel.hasAttr(NetSession.sessionKey())) {
            return;
        }

        NetSession session = channel.attr(NetSession.sessionKey()).get();
        sessionInactive(session);
        if (session != null) {
            this.appContext.publishEvent(new SessionInactiveEvent(this, session.userId()));
        }
    }

    protected void sessionInactive(Session session) {
        if (null == session) {
            return;
        }
        if (session.userId() != null) {
            USER_SESSION.remove(session.userId());
        }
        session.close();
    }

    public NetSession clearUserSession(Integer userId) {
        if (userId != null) {
            if (!USER_SESSION.containsKey(userId)) {
                return null;
            }
            NetSession session = USER_SESSION.remove(userId);
            log.debug("user session {} destroyed, remains {}", userId, USER_SESSION.size());
            return session;
        }

        return null;
    }

    public void broadcast(BaseMessage message) {
        for (Map.Entry<Integer, NetSession> sessionEntry : USER_SESSION.entrySet()) {
            NetSession session = sessionEntry.getValue();
            session.writeAndFlush(message);
        }
    }

    public void close(Channel channel) {
        if (!channel.hasAttr(NetSession.sessionKey())) {
            log.warn("channel has no session! {}", channel.id().asLongText());
            return;
        }

        NetSession session = channel.attr(NetSession.sessionKey()).get();

        if (session == null) {
            return;
        }

        this.clearUserSession(session.userId());

        this.appContext.publishEvent(new SessionClosedEvent(this, session.userId()));
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    public void bindUser(Integer userId, NetSession session) {
        session.setUserId(userId);
        USER_SESSION.put(userId, session);
    }
}

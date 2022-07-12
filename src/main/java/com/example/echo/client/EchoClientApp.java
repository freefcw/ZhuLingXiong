package com.example.echo.client;

public class EchoClientApp {
    public static void main(String[] args) throws InterruptedException {
        String host = "127.0.0.1";
        Integer port = 7769;
        String authKey = "HsTK2Y4fdIx3ZM9xEOX4Nc0rDePvZHxM";
        EchoClient client = new EchoClient(host, port, authKey);
        Integer userId = 24202421;
        client.run(userId, new UserInputSource());
        client.close();
    }
}

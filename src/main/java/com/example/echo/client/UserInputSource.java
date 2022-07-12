package com.example.echo.client;

import java.util.Objects;
import java.util.Scanner;

public class UserInputSource implements InputSource {
    @Override
    public void handle(MessageSender messageSender) {
        while (true) {
            System.out.println("Now enter your message:");
            Scanner scanner = new Scanner(System.in);
            String next = scanner.nextLine();
            if (Objects.equals(next, "exit")) {
                return;
            }
            messageSender.send(next);
        }
    }
}

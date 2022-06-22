package StructurePackage;

import ShopPackage.Command;

public class Sender {
    public static void sendMessage(int target, String message, Command command) {
        System.out.println("User " + target + " received " + message + ". Command = " +
                command + " Thread = "+ Thread.currentThread().getName());
    }
}

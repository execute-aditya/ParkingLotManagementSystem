package com.parking.util;

import java.util.Scanner;

/**
 * Helper methods for clean console I/O.
 */
public class ConsoleHelper {

    private static final Scanner scanner = new Scanner(System.in);

    public static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Please try again.");
            }
        }
    }

    public static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Please try again.");
            }
        }
    }

    public static void printLine() {
        System.out.println("═".repeat(62));
    }

    public static void printHeader(String title) {
        printLine();
        System.out.printf("  %-58s%n", title);
        printLine();
    }

    public static void printSuccess(String msg) {
        System.out.println("  ✔  " + msg);
    }

    public static void printError(String msg) {
        System.out.println("  ✘  ERROR: " + msg);
    }

    public static void printInfo(String msg) {
        System.out.println("  ℹ  " + msg);
    }
}

package me.brandonjja;

import java.io.PrintWriter;

public class JawmLogger {

    private static final PrintWriter PW = new PrintWriter(System.out);
    private static boolean verbose = false;

    public static void verbose() {
        verbose = true;
    }

    public static void log(String message) {
        if (verbose) {
            PW.println("[JAWM] " + message);
            PW.flush();
        }
    }
}

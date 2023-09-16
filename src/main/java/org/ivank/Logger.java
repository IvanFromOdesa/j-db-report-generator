package org.ivank;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * A simple logger class.
 */
public final class Logger {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Logger.class.getName());

    static {
        Handler fileHandler, consoleHandler = new ConsoleHandler();
        try {
            LogManager.getLogManager().readConfiguration();
            // Specify the java.util.logging.FileHandler.pattern in custom logging.properties
            fileHandler = new FileHandler();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOGGER.addHandler(fileHandler);
        LOGGER.addHandler(consoleHandler);
        info("Logger initialized successfully.");
    }

    public static void info(String msg) {
        LOGGER.log(Level.INFO, msg);
    }

    public static void error(Throwable e) {
        LOGGER.log(Level.SEVERE, e.getMessage(), e);
    }

    public static void error(String msg, Throwable e) {
        LOGGER.log(Level.SEVERE, msg, e);
    }

    public static void warning(String msg) {
        LOGGER.log(Level.WARNING, msg);
    }

    public static void config(String msg) {
        LOGGER.log(Level.CONFIG, msg);
    }
}

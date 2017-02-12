package top.rammer.multihighlight;

import com.intellij.openapi.diagnostic.Logger;

/**
 * Created by Rammer on 06/02/2017.
 */
public class Log {

    private static final boolean DEBUG = Boolean.getBoolean("MultiHighlight.debug");
    private static final Logger LOGGER = Logger.getInstance("MultiHighlight");

    public static void className(String msg, Object obj) {
        if (DEBUG) {
            if (obj == null) {
                LOGGER.info("instance == null -> " + msg);
            } else {
                LOGGER.info(msg + ": " + obj.getClass() + " -> " + obj);
            }
        }
    }

    public static void info(String msg) {
        if (DEBUG) {
            LOGGER.info(msg);
        }
    }

    public static void error(String msg) {
        if (DEBUG) {
            LOGGER.warn(msg);
        }
    }
}

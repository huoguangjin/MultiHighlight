package top.rammer.multihighlight;

/**
 * Created by Rammer on 06/02/2017.
 */
public class Log {

    private static final boolean DEBUG = true;

    public static boolean checkNull(String msg, Object obj) {
        if (obj == null) {
            if (DEBUG) {
                System.out.println(">>> warn: " + msg);
            }
            return true;
        } else {
            return false;
        }
    }

    public static void className(String msg, Object obj) {
        if (DEBUG) {
            if (obj == null) {
                System.out.println(">> info: instance == null -> " + msg);
            } else {
                System.out.println(">> info: " + msg + ": " + obj.getClass() + " -> " + obj);
            }
        }
    }

    public static void info(String msg) {
        if (DEBUG) {
            System.out.println("> info: " + msg);
        }
    }

    public static void error(String msg) {
        if (DEBUG) {
            System.out.println(">>>>> error: " + msg);
        }
    }
}

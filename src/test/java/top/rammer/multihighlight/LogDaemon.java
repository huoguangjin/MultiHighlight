package top.rammer.multihighlight;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/**
 * Created by Rammer on 12/02/2017.
 */
public class LogDaemon {

    @Test
    public void start() {
        try {
            // my idea is 2016.2 without gradle log config options
            // so config JUnit log running to watch log file
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

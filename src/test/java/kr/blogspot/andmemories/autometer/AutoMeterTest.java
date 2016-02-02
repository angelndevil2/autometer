package kr.blogspot.andmemories.autometer;

import org.junit.Test;

import java.io.IOException;

/**
 * @author k, Created on 16. 1. 26.
 */
public class AutoMeterTest {

    @Test
    public void MainTest() throws IOException {

        AutoMeter autoMeter = new AutoMeter();
        autoMeter.setDirs("src/dist");
        autoMeter.setNumOfThread(10);
        autoMeter.addHttpSampler("localhost", 8080, "/", "GET");
        autoMeter.setCollectRemoteSystemInfo(false);
        autoMeter.setLoopCount(10);
        autoMeter.doTest();
    }

    @Test
    public void saveJMX() throws IOException {
        AutoMeter autoMeter = new AutoMeter();
        autoMeter.setDirs("src/dist");

        autoMeter.setTestPlanName("test plan from java code");
        autoMeter.printTestPlanJmx();
    }
}

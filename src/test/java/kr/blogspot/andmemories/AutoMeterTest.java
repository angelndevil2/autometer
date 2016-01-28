package kr.blogspot.andmemories;

import org.junit.Test;

import java.io.IOException;

/**
 * @author k, Created on 16. 1. 26.
 */
public class AutoMeterTest {

    @Test
    public void MainTest() {
        AutoMeter.main(null);
    }

    @Test
    public void saveJMX() throws IOException {
        AutoMeter autoMeter = new AutoMeter();

        autoMeter.setTestPlanName("test plan form java code");
        autoMeter.printTestPlanJmx();
    }
}

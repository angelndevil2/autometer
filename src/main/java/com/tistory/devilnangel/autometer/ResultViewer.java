package com.tistory.devilnangel.autometer;

import com.tistory.devilnangel.autometer.common.IResultSaver;
import com.tistory.devilnangel.autometer.common.AbstractQ;
import com.tistory.devilnangel.autometer.common.AutoMeterException;
import com.tistory.devilnangel.autometer.common.StatisticSample;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Formatter;

/**
 * @author k, Created on 16. 1. 31.
 */
@Slf4j
@Data
public class ResultViewer implements Runnable {

    private boolean needClear = true;
    /**
     * save result to cvs file when test ended.
     */
    private IResultSaver saver = null;
    private long saveInterval = 30000L; // 30 sec.
    private long viewInterval = 5000L; // 5 sec.
    private long saveLastTime = System.currentTimeMillis();
    private long viewLastTime = System.currentTimeMillis();

    private final static String FORMAT =
            "%15s" +
            "%15d" +
            "%15.2f" +
            "%15d" +
            "%15d" +
            "%15.2f" +
            "%15.2f" +
            "%15.2f" +
            "%15.2f" +
            "%15.2f" +
            "%15.2f";
    private final static String HEADER = String.format(
            "%15s"+
            "%15s" +
            "%15s" +
            "%15s" +
            "%15s" +
            "%15s" +
            "%15s" +
            "%15s" +
            "%15s" +
            "%15s" +
            "%15s", "label", "#Samples", "Average", "Min", " Max", "Std. Dev.", "Error %", "Throughput", "KB/sec", "Avg. Bytes", "cpu");

    private final static String CSV_RESULT_HEADER = String.format(
            "%15s"+
                    "%15s" +
                    "%15s" +
                    "%15s" +
                    "%15s" +
                    "%15s" +
                    "%15s" +
                    "%15s" +
                    "%15s" +
                    "%15s" +
                    "%15s", "label", "#Samples", "Average", "Min", " Max", "Std. Dev.", "Error %", "Throughput", "KB/sec", "Avg. Bytes", "cpu");
    private final AbstractQ q = new AbstractQ() {};

    public boolean offer(Object obj) { return q.offer(obj); }

    public Object take() throws InterruptedException { return  q.take(); }

    public void show(Object obj) {
        if (needClear) {
            System.out.print("\033[H\033[2J");
            System.out.println();
            System.out.println();
            System.out.println(HEADER);
            needClear = false;

            if (saver != null) {
                saver.writeHeader(CSV_RESULT_HEADER);
            }
        }
        if (obj instanceof StatisticSample) {

            StatisticSample sample = (StatisticSample)obj;

            long currentTime = System.currentTimeMillis();
            long timeGap = currentTime - viewLastTime;

            if (timeGap >= viewInterval) {
                viewLastTime = currentTime;
                Formatter f = new Formatter();
                f.format(FORMAT,
                        sample.getName(),
                        sample.getCount(),
                        sample.getAvgRate(),
                        sample.getMin(),
                        sample.getMax(),
                        sample.getDeviation(),
                        sample.getErrorPercentage(),
                        sample.getRate(),
                        sample.getBytesPerSec() / 1024,
                        sample.getAvgPageBytes(),
                        sample.getCpuBusyPercentage());
                System.out.print(f);
                System.out.print('\r');
            }

            timeGap = currentTime - saveLastTime;
            if (saver != null && timeGap > saveInterval) {
                saveLastTime = currentTime;
                saver.save(sample);
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                show(take());
            } catch (InterruptedException e) {
                log.warn("interrupted ", e);
                return;
            }
        }
    }

    public Thread start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
        return t;
    }

    public void clear() throws AutoMeterException {
        needClear = true;
        if (saver != null) {
            saver.close();
            saver = null;
        }
    }
}

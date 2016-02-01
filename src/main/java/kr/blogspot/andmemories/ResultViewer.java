package kr.blogspot.andmemories;

import kr.blogspot.andmemories.common.AbstractQ;
import kr.blogspot.andmemories.common.StatisticSample;
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
        }
        if (obj instanceof StatisticSample) {
            Formatter f = new Formatter();
            f.format(FORMAT,
                    ((StatisticSample) obj).getName(),
                    ((StatisticSample) obj).getCount(),
                    ((StatisticSample) obj).getAvgRate(),
                    ((StatisticSample) obj).getMin(),
                    ((StatisticSample) obj).getMax(),
                    ((StatisticSample) obj).getDeviation(),
                    ((StatisticSample) obj).getErrorPercentage(),
                    ((StatisticSample) obj).getRate(),
                    ((StatisticSample) obj).getBytesPerSec() / 1024,
                    ((StatisticSample) obj).getAvgPageBytes(),
                    ((StatisticSample) obj).getCpuBusyPercentage());
            System.out.print(f);System.out.print('\r');
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                show(q.take());
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

    public void clear() {
        needClear = true;
    }
}

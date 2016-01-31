package kr.blogspot.andmemories;

import kr.blogspot.andmemories.common.AbstractQ;
import kr.blogspot.andmemories.common.StatisticSample;
import lombok.Data;
import lombok.extern.log4j.Log4j;

import java.util.Formatter;

/**
 * @author k, Created on 16. 1. 31.
 */
@Log4j
@Data
public class ResultViewer implements Runnable {

    private final static String FORMAT =
            "label=%s," +
            " #Samples=%d," +
            " Average=%.2f," +
            " Min=%d," +
            " Max=%d," +
            " Std. Dev.=%.2f," +
            " Error %%=%.2f," +
            " Throughput=%.2f," +
            " KB/sec=%.2f," +
            " Avg. Bytes=%.2f," +
            " cpu busy=%.2f";

    private final AbstractQ q = new AbstractQ() {};

    public boolean offer(Object obj) { return q.offer(obj); }

    public Object take() throws InterruptedException { return  q.take(); }

    public void show(Object obj) {

        if (obj instanceof StatisticSample) {
            System.out.print("\033[H\033[2J");
            System.out.println();
            System.out.println();
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
            System.out.println(f);
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
}

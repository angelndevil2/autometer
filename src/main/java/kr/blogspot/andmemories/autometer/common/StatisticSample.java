package kr.blogspot.andmemories.autometer.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Formatter;

/**
 * @author k, Created on 16. 1. 31.
 */
@Data
public class StatisticSample implements Serializable, Comparable<StatisticSample> {

    public static final String FORMAT = "%s,%d,%d,%.2f,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%d";

    private String name;
    private long timestamp;
    private long count;
    private double avgRate;
    private long min;
    private long max;
    private double deviation;
    private double errorPercentage;
    private double rate;
    private double bytesPerSec;
    private double avgPageBytes;
    private double cpuBusyPercentage;
    private long totalThread;

    public String toCVSString() {
        Formatter f = new Formatter();
        return f.format(FORMAT,
                name,
                timestamp,
                count,
                avgRate,
                min,
                max,
                deviation,
                errorPercentage,
                rate,
                bytesPerSec,
                avgPageBytes,
                cpuBusyPercentage,
                totalThread
                ).toString();
    }

    public int compareTo(StatisticSample o) {
        return this.count - o.count < 0L?-1:(this.count == o.count?0:1);
    }
}

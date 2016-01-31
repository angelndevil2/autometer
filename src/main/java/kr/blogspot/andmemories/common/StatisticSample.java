package kr.blogspot.andmemories.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author k, Created on 16. 1. 31.
 */
@Data
public class StatisticSample implements Serializable, Comparable<StatisticSample> {
    private String name;
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

    public int compareTo(StatisticSample o) {
        return this.count - o.count < 0L?-1:(this.count == o.count?0:1);
    }
}

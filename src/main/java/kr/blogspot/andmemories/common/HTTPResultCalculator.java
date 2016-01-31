package kr.blogspot.andmemories.common;

import kr.blogspot.andmemories.ResultViewer;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.Calculator;

import java.util.HashMap;

/**
 * @author k, Created on 16. 1. 31.
 */
@Log4j
@Data
public class HTTPResultCalculator implements Runnable {

    private final AbstractQ q;
    private final HashMap<String, Calculator> calculators = new HashMap<String, Calculator>();
    private final Calculator totalCalculator = new Calculator("TOTAL");
    private ResultViewer resultViewer;

    public HTTPResultCalculator() { this(AbstractQ.EQ_SIZE); }
    public HTTPResultCalculator(int qsize) {
        q = new AbstractQ(qsize) {};
    }

    @Override
    public void run() {

        while (true) {
            try {
                AutometerHTTPSampleResult sampleResult = (AutometerHTTPSampleResult) q.take();
                addResult(sampleResult);
            } catch (InterruptedException e) {
                log.warn("interrupted", e);
                return;
            }
        }
    }

    private void addResult(@NonNull final AutometerHTTPSampleResult sampleResult) {
        Calculator calculator = null;

        String path = sampleResult.getURL().getPath();
        if (path != null && !calculators.containsKey(path)) {
            calculators.put(path, new Calculator(path));
        }
        if (path != null) calculator = calculators.get(path);
        if (calculator != null) calculator.addSample(sampleResult);

        totalCalculator.addSample(sampleResult);
        StatisticSample sample = new StatisticSample();
        sample.setName(totalCalculator.getLabel());
        sample.setRate(totalCalculator.getRate());
        sample.setAvgRate(totalCalculator.getMean());
        sample.setDeviation(totalCalculator.getStandardDeviation());
        sample.setErrorPercentage(totalCalculator.getErrorPercentage());
        sample.setBytesPerSec(totalCalculator.getBytesPerSecond());
        sample.setAvgPageBytes(totalCalculator.getAvgPageBytes());
        sample.setCpuBusyPercentage(sampleResult.getCpuBusy());
        sample.setCount(totalCalculator.getCount());
        sample.setMax(totalCalculator.getMax());
        sample.setMin(totalCalculator.getMin());
        if (resultViewer != null) resultViewer.offer(sample);

    }

    public Thread start() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
        return t;
    }

    public void clear() {
        doEndJob();
        resultViewer.clear();
        totalCalculator.clear();
        calculators.clear();
        q.clear();
    }

    private void doEndJob() {

    }

    public boolean offer(SampleResult sampleResult) { return q.offer(sampleResult); }
}

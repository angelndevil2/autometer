package kr.blogspot.andmemories.reporters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.SamplingStatCalculator;

import java.util.Formatter;
import java.util.HashMap;

/**
 * @author k, Created on 16. 1. 29.
 */
@Log4j
@EqualsAndHashCode(callSuper = true)
public @Data class AutoMeterResultCollector extends ResultCollector {

    private final SamplingStatCalculator calculator = new SamplingStatCalculator();
    private final Summariser summariser;
    private transient HashMap<String, SystemInfoCollector> httpDomains;
    private final static String FORMAT =
            "label=%s," +
            " #Samples=%s," +
            " Average=%.2f," +
            " Min=%d," +
            " Max=%d," +
            " Std. Dev.=%.2f," +
            " Error %%=%.2f," +
            " Throughput=%.2f," +
            " KB/sec=%.2f," +
            " Avg. Bytes=%.2f," +
            " cpu busy=%.2f";

    public AutoMeterResultCollector(Summariser summer) {
        super(summer);
        summariser = summer;
    }

    @Override
    public void testStarted(String host) {
        calculator.clear();
        super.testStarted(host);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        SampleResult r = e.getResult();
        if (isSampleWanted(r.isSuccessful())) {
            String host = r.getURL().getHost();
            SystemInfoCollector sic = httpDomains == null || host == null ? null:httpDomains.get(host);
            calculator.addSample(r);
            Formatter f = new Formatter();
            f.format(FORMAT,
                    host == null ? "": host,
                    calculator.getCount(),
                    calculator.getMean(),
                    calculator.getMin().longValue(),
                    calculator.getMax().longValue(),
                    calculator.getStandardDeviation(),
                    calculator.getErrorPercentage(),
                    calculator.getRate(),
                    calculator.getBytesPerSecond()/1024,
                    calculator.getAvgPageBytes(),
                    sic == null?0F:sic.getCpuBusy());
            System.out.print("\033[H\033[2J");
            System.out.println();
            System.out.println();
            System.out.print(f);
        }

        super.sampleOccurred(e);
    }
}

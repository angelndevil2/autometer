package kr.blogspot.andmemories.reporters;

import kr.blogspot.andmemories.common.AutometerHTTPSampleResult;
import kr.blogspot.andmemories.common.HTTPResultCalculator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;

import java.util.HashMap;

/**
 * @author k, Created on 16. 1. 29.
 */
@Log4j
@EqualsAndHashCode(callSuper = true)
public @Data class AutoMeterResultCollector extends ResultCollector {

    private transient HTTPResultCalculator calculator;
    private final Summariser summariser;
    private transient HashMap<String, SystemInfoCollector> httpDomains;

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
    public void sampleOccurred(final SampleEvent e) {
        SampleResult r = e.getResult();
        if (isSampleWanted(r.isSuccessful())) {
            String host = r.getURL().getHost();
            //String path = r.getURL().getPath();
            SystemInfoCollector sic = httpDomains == null || host == null ? null : httpDomains.get(host);
            if (r instanceof HTTPSampleResult) {
                AutometerHTTPSampleResult result = new AutometerHTTPSampleResult((HTTPSampleResult) r);
                result.setCpuBusy(sic == null ? 0D:sic.getCpuBusy());
                if (!calculator.offer(result)) log.error("calculating queue is full.");
            }


            /*System.out.print("\033[H\033[2J");
            System.out.println();
            System.out.println();
            for (String p : calculators.keySet()) {
                Formatter f = new Formatter();
                f.format(FORMAT,
                        p,
                        calculators.get(p).getCount(),
                        calculators.get(p).getMean(),
                        calculators.get(p).getMin(),
                        calculators.get(p).getMax(),
                        calculators.get(p).getStandardDeviation(),
                        calculators.get(p).getErrorPercentage(),
                        calculators.get(p).getRate(),
                        calculators.get(p).getBytesPerSecond() / 1024,
                        calculators.get(p).getAvgPageBytes(),
                        sic == null ? 0F : sic.getCpuBusy());
                System.out.println(f);
            }*/
        }

        super.sampleOccurred(e);
    }
}

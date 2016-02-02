package kr.blogspot.andmemories.autometer.reporters;

import kr.blogspot.andmemories.autometer.common.AutoMeterException;
import kr.blogspot.andmemories.autometer.common.AutometerHTTPSampleResult;
import kr.blogspot.andmemories.autometer.common.HTTPResultCalculator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;

import java.util.HashMap;

/**
 * @author k, Created on 16. 1. 29.
 */
@Slf4j
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
    public void testEnded(String host) {
        try {
            calculator.clear();
        } catch (AutoMeterException e) {
            log.error("clear exception.",e);
        }
        super.testEnded(host);
    }

    @Override
    public void sampleOccurred(final SampleEvent e) {
        SampleResult r = e.getResult();
        if (isSampleWanted(r.isSuccessful())) {
            String host = r.getURL().getHost();
            SystemInfoCollector sic = httpDomains == null || host == null ? null : httpDomains.get(host);
            if (r instanceof HTTPSampleResult) {
                AutometerHTTPSampleResult result = new AutometerHTTPSampleResult((HTTPSampleResult) r);
                result.setCpuBusy(sic == null ? 0D:sic.getCpuBusy());
                if (!calculator.offer(result)) log.error("calculating queue is full.");
            }
        }

        //super.sampleOccurred(e);
    }
}

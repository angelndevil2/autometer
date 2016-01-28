package kr.blogspot.andmemories.reporters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Formatter;

/**
 * @author k, Created on 16. 1. 29.
 */
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public @Data class AutoMeterResultCollector extends ResultCollector {

    private final static String FORMAT = "thread name=%s, url=%s, latency=%d, connect=%d, idle=%d, response time=%d, threads=%d";

    public AutoMeterResultCollector(Summariser summer) {
        super(summer);
    }

    @Override
    public void sampleOccurred(SampleEvent e) {
        super.sampleOccurred(e);
        SampleResult r = e.getResult();
        if (r.isSuccessful()) {
            Formatter f = new Formatter();
            f.format(FORMAT, r.getThreadName(), r.getURL().getPath(), r.getLatency(), r.getConnectTime(), r.getIdleTime(), r.getTime(), r.getAllThreads());
            System.out.println(f);
        }

    }

}

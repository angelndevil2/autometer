package kr.blogspot.andmemories.common;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;

/**
 * @author k, Created on 16. 1. 31.
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class AutometerHTTPSampleResult extends HTTPSampleResult {

    private double cpuBusy;

    public AutometerHTTPSampleResult() {
        super();
        cpuBusy = 0D;
    }

    public AutometerHTTPSampleResult(HTTPSampleResult sampleResult) {
        super(sampleResult);
        cpuBusy = 0D;
    }

    public AutometerHTTPSampleResult(AutometerHTTPSampleResult sampleResult) {
        super(sampleResult);
        cpuBusy = sampleResult.getCpuBusy();
    }
}

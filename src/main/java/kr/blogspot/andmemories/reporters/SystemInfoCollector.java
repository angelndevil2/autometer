package kr.blogspot.andmemories.reporters;

import com.tistory.devilnangel.client.RmiSystemInfoClient;
import com.tistory.devilnangel.common.IRmiCpuInfo;
import lombok.Data;
import lombok.extern.log4j.Log4j;

import java.rmi.RemoteException;

/**
 *
 * Collect system information using <a href="https://github.com/angelndevil2/system-info">system-info</a>
 *
 * @author k, Created on 16. 1. 31.
 */
@Data
@Log4j
public class SystemInfoCollector implements Runnable {

    public static final long COLLECT_INTERVAL = 1000;
    private IRmiCpuInfo cpuInfo;
    private final String domain;
    private volatile double cpuBusy = 0D;

    public SystemInfoCollector(final String host) {
        domain = host;
    }

    @Override
    public void run() {

        if (domain == null) return;

        try {
            cpuInfo = new RmiSystemInfoClient(domain).getCpuInfo();
        } catch (Exception e) {
            log.error(domain+" exception ", e);
            return;
        }

        while (true) {
            try {
                cpuBusy = cpuInfo.getCpuBusy();
                Thread.sleep(COLLECT_INTERVAL);
            } catch (RemoteException e) {
                log.error(domain+" remote exception ", e);
                return;
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public Thread start() {
        Thread t = new Thread(this);
        t.start();
        return t;
    }
}

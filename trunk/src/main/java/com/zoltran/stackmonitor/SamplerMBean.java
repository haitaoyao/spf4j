/**
 * (c) Zoltan Farkas 2012
 */
package com.zoltran.stackmonitor;

import com.zoltran.base.ReportGenerator;
import java.io.IOException;
import javax.annotation.PreDestroy;

/**
 *
 * @author zoly
 */
public interface SamplerMBean extends ReportGenerator{

    void generateHtmlMonitorReport(String fileName, int chartWidth) throws IOException;

    void start();

    @PreDestroy
    void stop() throws InterruptedException;
    
    void clear();

    long getSampleTimeMillis();
    
    void setSampleTimeMillis(long sampleTimeMillis) ;

    boolean isStopped();
    
}

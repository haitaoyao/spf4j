/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zoltran.stackmonitor;

import com.google.common.base.Function;
import javax.annotation.concurrent.GuardedBy;

/**
 *
 * @author zoly
 */
public abstract class AbstractStackCollector implements StackCollector {
 
    
    protected final Object sampleSync = new Object();
    @GuardedBy(value = "sampleSync")
    private SampleNode samples;

    
    @Override
    public SampleNode applyOnSamples(Function<SampleNode, SampleNode> predicate) {
        synchronized (sampleSync) {
            return samples = predicate.apply(samples);
        }
    }


    @Override
    public void clear() {
        synchronized (sampleSync) {
            samples = null;
        }
    }
    
    protected void addSample(StackTraceElement[] stackTrace) {
        synchronized (sampleSync) {
            if (samples == null) {
                samples = new SampleNode(stackTrace, stackTrace.length - 1);
            } else {
                samples.addSample(stackTrace, stackTrace.length - 1);
            }
        }
    }
    
    
}

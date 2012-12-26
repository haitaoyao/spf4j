/**
 * (c) Zoltan Farkas 2012
 */
package com.zoltran.perf.impl;

import com.google.common.math.IntMath;
import com.zoltran.perf.EntityMeasurements;
import com.zoltran.perf.EntityMeasurementsInfo;
import com.zoltran.perf.MeasurementProcessor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.ThreadSafe;

/**
 *
 * @author zoly
 */
@ThreadSafe
public class QuantizedRecorder implements MeasurementProcessor, Cloneable {

    private long minMeasurement;
    private long maxMeasurement;
    private long measurementCount;
    private long measurementTotal;
    private final int quantasPerMagnitude;
    private final long[] magnitudes;
    private final EntityMeasurementsInfo info;
    private final int factor;
    private final int lowerMagnitude;
    private final int higherMagnitude;
    /**
     * (long.min - f ^ l), (f ^ l - f ^ (l+1)), ... (f ^(L-1) - f ^ L) (f^L -
     * long.max)
     *
     * f = 10 m = 0 M = 5
     *
     */
    private final long[] quatizedMeasurements;

    public QuantizedRecorder(final Object measuredEntity, final String unitOfMeasurement, int factor, int lowerMagnitude,
            int higherMagnitude, final int quantasPerMagnitude) {
        assert (quantasPerMagnitude <= factor);
        assert (lowerMagnitude < higherMagnitude);
        assert (quantasPerMagnitude > 0);
        this.factor = factor;
        this.lowerMagnitude = lowerMagnitude;
        this.higherMagnitude = higherMagnitude;
        minMeasurement = Long.MAX_VALUE;
        maxMeasurement = Long.MIN_VALUE;
        measurementCount = 0;
        measurementTotal = 0;
        this.quatizedMeasurements = new long[(higherMagnitude - lowerMagnitude) * quantasPerMagnitude + 2];
        this.quantasPerMagnitude = quantasPerMagnitude;
        magnitudes = new long[higherMagnitude - lowerMagnitude + 1];

        int idx = 0;
        if (lowerMagnitude < 0) {
            int toMagnitude = Math.min(-1, higherMagnitude);
            int toValue = -IntMath.pow(factor, -toMagnitude);
            int j = idx = toMagnitude - lowerMagnitude;
            while (j >= 0) {
                magnitudes[j--] = toValue;
                toValue *= factor;
            }
            idx++;
        }
        if (lowerMagnitude <= 0 && higherMagnitude >= 0) {
            magnitudes[idx++] = 0;
        }

        int fromMagnitude = Math.max(1, lowerMagnitude);
        int fromValue = IntMath.pow(factor, fromMagnitude);
        int j = idx;
        while (j < magnitudes.length) {
            magnitudes[j++] = fromValue;
            fromValue *= factor;
        }
        final Set<String> result = new HashSet<String>();
        result.add("total");
        result.add("count");
        result.add("min");
        result.add("max");

        result.add("QNI_" + magnitudes[0]);
        if (magnitudes.length > 0) {
            long prevVal = magnitudes[0];
            for (int i = 1; i < magnitudes.length; i++) {
                long magVal = magnitudes[i];
                long intSize = magVal - prevVal;
                for (j = 0; j < quantasPerMagnitude; j++) {
                    result.add("Q" + (prevVal + intSize * j / quantasPerMagnitude)
                            + "_" + (prevVal + intSize * (j + 1) / quantasPerMagnitude));
                }
                prevVal = magVal;
            }
            result.add("Q" + prevVal
                    + "_PI");
        }
        info = new EntityMeasurementsInfoImpl(measuredEntity, unitOfMeasurement, result);

    }

    private QuantizedRecorder(EntityMeasurementsInfo info, int factor, int lowerMagnitude, int higherMagnitude,
            long minMeasurement, long maxMeasurement, long measurementCount, long measurementTotal, 
            int quantasPerMagnitude, long[] magnitudes, long[] quatizedMeasurements) {
        assert (quantasPerMagnitude <= factor);
        assert (lowerMagnitude < higherMagnitude);
        assert (quantasPerMagnitude > 0);
        this.factor = factor;
        this.lowerMagnitude = lowerMagnitude;
        this.higherMagnitude = higherMagnitude;
        this.minMeasurement = minMeasurement;
        this.maxMeasurement = maxMeasurement;
        this.measurementCount = measurementCount;
        this.measurementTotal = measurementTotal;
        this.quantasPerMagnitude = quantasPerMagnitude;
        this.magnitudes = magnitudes;
        this.quatizedMeasurements = quatizedMeasurements;
        this.info = info;
    }

    public synchronized void record(long measurement) {
        measurementCount++;
        measurementTotal += measurement;  // TODO: check for overflow
        if (measurement < minMeasurement) {
            minMeasurement = measurement;
        }
        if (measurement > maxMeasurement) {
            maxMeasurement = measurement;
        }
        long m0 = magnitudes[0];
        if (m0 > measurement) {
            quatizedMeasurements[0]++;
        } else {
            long prevMag = m0;
            int i = 1;
            for (; i < magnitudes.length; i++) {
                long mag = magnitudes[i];
                if (mag > measurement) {
                    int qidx = (i - 1) * quantasPerMagnitude + (int) (quantasPerMagnitude
                            * (((double) (measurement - prevMag)) / (mag - prevMag))) + 1;
                    quatizedMeasurements[qidx]++;
                    break;
                }
                prevMag = mag;
            }
            if (i == magnitudes.length) {
                quatizedMeasurements[quatizedMeasurements.length - 1]++;
            }


        }

    }

    @Override
    public synchronized Map<String, Number> getMeasurements(boolean reset) {
        Map<String, Number> result = new HashMap<String, Number>();
        result.put("total", this.measurementTotal);
        result.put("count", this.measurementCount);
        if (this.measurementCount > 0) {
            result.put("min", this.minMeasurement);
            result.put("max", this.maxMeasurement);
        }

        result.put("QNI_" + this.magnitudes[0], this.quatizedMeasurements[0]);
        if (magnitudes.length > 0) {
            int k = 1;
            long prevVal = magnitudes[0];
            for (int i = 1; i < magnitudes.length; i++) {
                long magVal = magnitudes[i];
                long intSize = magVal - prevVal;
                for (int j = 0; j < quantasPerMagnitude; j++) {
                    result.put("Q" + (prevVal + intSize * j / quantasPerMagnitude)
                            + "_" + (prevVal + intSize * (j + 1) / quantasPerMagnitude), this.quatizedMeasurements[k++]);
                }
                prevVal = magVal;
            }
            result.put("Q" + prevVal
                    + "_PI", this.quatizedMeasurements[k]);
        }
        if (reset) {
            reset();
        }
        return result;
    }

    @Override
    public EntityMeasurementsInfo getInfo() {
        return info;
    }

    @Override
    public synchronized EntityMeasurements aggregate(EntityMeasurements mSource) {

        QuantizedRecorder other = (QuantizedRecorder) mSource;
        synchronized (other) {
            long[] quantizedM = quatizedMeasurements.clone();
            for (int i = 0; i < quantizedM.length; i++) {
                quantizedM[i] += other.quatizedMeasurements[i];
            }

            return new QuantizedRecorder(info,factor, lowerMagnitude, higherMagnitude,
                    Math.min(this.minMeasurement, other.minMeasurement),
                    Math.max(this.maxMeasurement, other.maxMeasurement),
                    this.measurementCount + other.measurementCount,
                    this.measurementTotal + other.measurementTotal,
                    quantasPerMagnitude, magnitudes, quantizedM);
        }


    }

    @Override
    public synchronized MeasurementProcessor createClone(boolean reset) {
        QuantizedRecorder result = new QuantizedRecorder(info,factor, lowerMagnitude, higherMagnitude,
                minMeasurement, maxMeasurement, measurementCount, measurementTotal,
                quantasPerMagnitude, magnitudes, quatizedMeasurements.clone());
        if (reset) {
            reset();
        }
        return result;
    }

    private void reset() {
        this.minMeasurement = Long.MAX_VALUE;
        this.maxMeasurement = Long.MIN_VALUE;
        this.measurementCount = 0;
        this.measurementTotal = 0;
        Arrays.fill(this.quatizedMeasurements, 0L);
    }

    @Override
    public String toString() {
        return "QuantizedRecorder{" + "info=" + info + ", minMeasurement=" + minMeasurement + ", maxMeasurement="
                + maxMeasurement + ", measurementCount=" + measurementCount + ", measurementTotal="
                + measurementTotal + ", quantasPerMagnitude=" + quantasPerMagnitude + ", magnitudes="
                + Arrays.toString(magnitudes) + ", quatizedMeasurements="
                + Arrays.toString(quatizedMeasurements) + '}';
    }

    public synchronized long getMaxMeasurement() {
        return maxMeasurement;
    }

    public synchronized long getMeasurementCount() {
        return measurementCount;
    }

    public synchronized long getMeasurementTotal() {
        return measurementTotal;
    }

    public synchronized long getMinMeasurement() {
        return minMeasurement;
    }

    public synchronized long[] getQuatizedMeasurements() {
        return quatizedMeasurements.clone();
    }

    @Override
    public synchronized EntityMeasurements createLike(Object entity) {
        QuantizedRecorder result = new QuantizedRecorder(entity, info.getUnitOfMeasurement(),
                this.factor, lowerMagnitude, higherMagnitude, quantasPerMagnitude);
        return result;
    }
}

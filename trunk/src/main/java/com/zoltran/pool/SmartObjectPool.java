/**
 * (c) Zoltan Farkas 2012
 */
package com.zoltran.pool;

import java.io.Closeable;
import java.util.concurrent.TimeoutException;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author zoly
 */
@ParametersAreNonnullByDefault
public interface SmartObjectPool<T> extends Disposable {
    
    T borrowObject(ObjectBorower borower) throws InterruptedException, TimeoutException;
    
    void returnObject(T object, ObjectBorower borower);
    
}

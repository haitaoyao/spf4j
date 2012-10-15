/**
 * (c) Zoltan Farkas 2012
 */
package com.zoltran.pool;

import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Object pool interface.
 * 
 * My goal is to create a simpler and better object pool interface and implementation.
 * 
 * 
 * @author zoly
 */
@ParametersAreNonnullByDefault
public interface ObjectPool<T>  extends Disposable {
    
    @Nonnull
    T borrowObject() throws ObjectCreationException, 
        InterruptedException, TimeoutException;
      
    void returnObject(T object, @Nullable Exception e)  
            throws TimeoutException, InterruptedException;
    
    
    public interface Handler<T> {
        
        void handle(T object);
        
    }
    
    public interface Factory<T> {
    
        T create() throws ObjectCreationException;
    
        void dispose(T object) throws TimeoutException, InterruptedException;
        
        boolean validate(T object, @Nullable Exception e);
        
    }
    
}

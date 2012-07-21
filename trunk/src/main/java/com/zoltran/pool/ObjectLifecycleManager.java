/**
 * (c) Zoltan Farkas 2012
 */
package com.zoltran.pool;

/**
 *
 * @author zoly
 */
public interface ObjectLifecycleManager<T> {
    
    T create();
    
    void dispose(T object);
    
    void beforeBorrow(T object);
    
    void afterReturn(T object);
        
    boolean validate(T object);
    
    Action decide(Exception e);
        
    enum Action {NOTHING, VALIDATE, VALIDATE_ALL};
    
    
}

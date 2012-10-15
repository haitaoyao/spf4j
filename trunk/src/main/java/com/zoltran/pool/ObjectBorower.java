/**
 * (c) Zoltan Farkas 2012
 */
package com.zoltran.pool;

import javax.annotation.Nullable;

/**
 *
 * @author zoly
 */
public interface ObjectBorower<T> extends Scanable<T>
{
    
    @Nullable
    T requestReturnObject();
    
    @Nullable
    T returnObjectIfAvailable() throws InterruptedException;
    

}

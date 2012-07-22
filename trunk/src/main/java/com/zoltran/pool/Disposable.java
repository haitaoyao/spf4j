/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zoltran.pool;

import java.util.concurrent.TimeoutException;

/**
 *
 * @author zoly
 */
public interface Disposable {
    
    public void dispose() throws TimeoutException, InterruptedException;
}

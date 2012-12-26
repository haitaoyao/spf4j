 /*
 * Copyright (c) 2001, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.zoltran.pool.impl;

import java.io.Closeable;
import java.io.IOException;

/**
 * This is a pooled object implementation, that behaves like a connection object. 
 * @author zoly
 */

public class ExpensiveTestObject implements Closeable
{
    private final long maxIdleMillis;
    
    private final int nrUsesToFailAfter;
    
    private final long minOperationMillis;
    
    private final long maxOperationMillis;
    
    private long lastTouchedTimeMillis;
    
    private int nrUses;

    public ExpensiveTestObject(long maxIdleMillis, int nrUsesToFailAfter, long minOperationMillis, long maxOperationMillis)
    {
        this.maxIdleMillis = maxIdleMillis;
        this.nrUsesToFailAfter = nrUsesToFailAfter;
        this.minOperationMillis = minOperationMillis;
        this.maxOperationMillis = maxOperationMillis;
        lastTouchedTimeMillis = System.currentTimeMillis();
        nrUses = 0;
        simulateDoStuff();
    }
    
    
    public void doStuff() throws IOException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTouchedTimeMillis > maxIdleMillis) {
            throw new IOException("Connection timed out");
        }
        if (nrUses > nrUsesToFailAfter) {
            throw new IOException("Simulated random crap");
        }
        simulateDoStuff();
        nrUses ++;
        lastTouchedTimeMillis = System.currentTimeMillis();       
    }

    @Override
    public void close() throws IOException {
        doStuff();
    }

    private void simulateDoStuff() throws RuntimeException {
        long sleepTime = (long) (Math.random() * (maxOperationMillis - minOperationMillis));
        try {
            Thread.sleep(minOperationMillis + sleepTime);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
}

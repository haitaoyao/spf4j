/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zoltran.pool.impl;

import com.zoltran.pool.Disposable;
import com.zoltran.pool.ObjectBorower;
import com.zoltran.pool.ObjectFactory;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author zoly
 */
public class SimpleSmartObjectPoolTest implements ObjectBorower<SimpleSmartObjectPoolTest.TestObject> {

    
    private TestObject borowedObject = null;
      
    private SimpleSmartObjectPool<TestObject> instance = new SimpleSmartObjectPool(10, new ObjectFactory<TestObject>() {
           @Override
            public TestObject create() {
               System.out.println("Creating Object");
               return new TestObject("Object");
            }

            @Override
            public void dispose(TestObject object) {
                try {
                     System.out.println("Disposing Object");
                    object.dispose();
                } catch (TimeoutException ex) {
                    throw new RuntimeException(ex);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }, 10000, true);
 
            
    @Override
    public TestObject requestReturnObject() {
        if (borowedObject != null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            instance.returnObject(borowedObject, this);
        }
        return null;   
    }

    @Override
    public TestObject returnObjectIfAvailable(){
        return borowedObject;
    }

    @Override
    public void dispose() throws TimeoutException, InterruptedException {
        if (borowedObject != null) {
            instance.returnObject(borowedObject, this);
        }
    }
    
    
    public static class TestObject implements Disposable {
    
    private boolean disposed = false;
    
    private final String data;

        public TestObject(String data) {
            this.data = data;
        }

        public String getData() {
            if (!disposed)
                return data;
            else 
                throw new RuntimeException(data + " is already disposed");
        }

        @Override
        public void dispose() throws TimeoutException, InterruptedException {
            disposed = true;
        }
    
        
    
    
    }
    
   

    /**
     * Test of borrowObject method, of class SimpleSmartObjectPool.
     */
    @Test
    public void testPool() throws Exception {
        System.out.println("borrowObject");
        borowedObject = instance.borrowObject(this);
        instance.returnObject(borowedObject, this);
        instance.dispose();
    }

 
}

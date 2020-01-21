/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.core.util;

import org.springframework.context.ApplicationContext;
import org.webcurator.core.exceptions.WCTRuntimeException;

/**
 * The ApplicationContextFactory holds a reference to the ApplicationContext so that
 * the Spring ApplicationContext can be accessed by objects that do not have access
 * to the ServletContext.
 * @author nwaight
 */
public class ApplicationContextFactory {
    /** the mutex for this singleton. */
    private static final Object mutex = new Object();
    /** the singleton instance of the ApplicationContextFactory. */
    private static ApplicationContextFactory instance = null;
    /** the spring application context. */
    private ApplicationContext ctx = null;
    
    /**
     * private constructor taking the ApplicationContext
     * @param argCtx the applications ApplicationContext
     */ 
    private ApplicationContextFactory(ApplicationContext argCtx) {
        super();
        ctx = argCtx;
    }
    
    /**
     * Set the ApplicationContext.
     * @param argCtx the ApplicationContext to set
     */
    public static void setApplicationContext(ApplicationContext argCtx) {
        synchronized (mutex) {
            if (instance == null) {
                instance = new ApplicationContextFactory(argCtx);
            }
            else {
                instance.ctx = argCtx;
            }
        }
    }

    /**
     * @return the ApplicationContext.
     */
    public static ApplicationContext getApplicationContext() {
       return instance.getContext();
    }
    
    /** 
     * Return the ApplicationContext stored by this instance.
     * @return the ApplicationContext
     */
    private ApplicationContext getContext() {
        if (ctx != null) {
            return ctx;
        }
        
        throw new WCTRuntimeException("The ApplicationContextFactory has not been initialised.");
    }    
}

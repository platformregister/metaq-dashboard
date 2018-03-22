package com.sohu.metaq.monitor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xiaofeiliu on 2015/6/11.
 */
public class ProberManager {

    private final static Logger logger = LoggerFactory.getLogger(ProberManager.class);

    private final Map<Class<? extends Prober>, Prober> probers = new HashMap<Class<? extends Prober>, Prober>();

    private final AtomicBoolean isInited = new AtomicBoolean(false);
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public void  registerProber(Prober prober) {
        this.probers.put(prober.getClass(), prober);
    }

    public void initProber() throws Exception {
        if(isInited.compareAndSet(false, true)) {

            try {

                for (Prober prober : this.probers.values()) {
                    prober.init();
                    logger.info(prober.getClass().getSimpleName() + " init success!");
                }

            } catch (Exception e) {
                throw new Exception(e);
            }
        }
    }

    public void startProber() throws Exception {
        if (this.isInited.get()) {
            for (Prober prober : this.probers.values()) {
                prober.startProb();
            }
            this.isStarted.set(true);
        } else {
            throw new Exception("ProberManager's startProb is error.");
        }
    }

    public void stopProber() throws Exception {
        if (this.isInited.get()) {
            for (Prober prober : this.probers.values()) {
                prober.stopProb();
            }
            this.isStarted.set(false);
        }
        else {
            throw new Exception("roberManager's stopProb is error.");
        }
    }



}

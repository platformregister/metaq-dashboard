package com.sohu.metaq.monitor.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by xiaofeiliu on 2015/6/12.
 */
abstract public class AbstractProber implements Prober {

    private final static Logger logger = LoggerFactory.getLogger(ProberManager.class);

    private volatile AtomicBoolean isProbeStarted = new AtomicBoolean(false);

    @Override
    public void startProb() throws InterruptedException {
        // 避免被误调用多次
        if (this.isProbeStarted.get() == false) {
            this.doStartProb();
            this.isProbeStarted.set(true);
        } else {
            this.logger.info("已经运行中,不必启动");
        }
    }

    @Override
    public void stopProb() {
        if (this.isProbeStarted.compareAndSet(true, false)) {
            this.doStopProb();
            this.logger.info("停止探测.");
        } else {
            this.logger.info("没有启动,不必停止");
        }
    }


    protected abstract void doStopProb();


    protected abstract void doStartProb() throws InterruptedException;



}

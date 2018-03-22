package com.sohu.metaq.monitor.core;

/**
 * Created by xiaofeiliu on 2015/6/11.
 */
public interface Prober {

    public void init();

    public void startProb() throws InterruptedException;

    public void stopProb();


}

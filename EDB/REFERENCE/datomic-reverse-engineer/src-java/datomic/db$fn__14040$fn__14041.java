/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class db$fn__14040$fn__14041
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");
    public static final Var const__1 = RT.var((String)"datomic.core2.thread", (String)"daemon-factory");

    public Object invoke() {
        ThreadPoolExecutor threadPoolExecutor;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.prefetchProbes");
        if (object != null && object != Boolean.FALSE) {
            String name = "probes";
            Object nthreads = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.prefetchConcurrency");
            LinkedBlockingQueue q2 = new LinkedBlockingQueue();
            String string = name;
            name = null;
            Object factory = ((IFn)const__1.getRawRoot()).invoke((Object)string);
            int n = RT.uncheckedIntCast((Object)nthreads);
            Object object2 = nthreads;
            nthreads = null;
            LinkedBlockingQueue linkedBlockingQueue = q2;
            q2 = null;
            Object object3 = factory;
            factory = null;
            ThreadPoolExecutor G__14042 = new ThreadPoolExecutor(n, RT.uncheckedIntCast((Object)object2), 30L, TimeUnit.SECONDS, (BlockingQueue<Runnable>)linkedBlockingQueue, (ThreadFactory)object3);
            G__14042.allowCoreThreadTimeOut(Boolean.TRUE);
            threadPoolExecutor = G__14042;
            G__14042 = null;
        } else {
            threadPoolExecutor = null;
        }
        return threadPoolExecutor;
    }
}


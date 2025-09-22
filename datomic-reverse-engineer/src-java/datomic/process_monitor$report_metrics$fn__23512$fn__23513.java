/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class process_monitor$report_metrics$fn__23512$fn__23513
extends AFunction {
    Object callback;
    Object m;
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"MetricsReport");
    public static final Object const__2 = 1L;
    public static final Var const__3 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__4 = RT.var((String)"datomic.slf4j", (String)"caused-by");
    public static final Keyword const__5 = RT.keyword(null, (String)"MetricsFail");

    public process_monitor$report_metrics$fn__23512$fn__23513(Object object, Object object2) {
        this.callback = object;
        this.m = object2;
    }

    public Object invoke() {
        Object object;
        try {
            this.callback = null;
            this.m = null;
            ((IFn)this.callback).invoke(this.m);
            object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1, const__2);
        }
        catch (Throwable t2) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.process-monitor");
            Object t2 = null;
            Throwable ex = t2;
            if (logger.isInfoEnabled()) {
                logger.info((String)((IFn)const__3.getRawRoot()).invoke((Object)"Unable to send metrics"), ex);
                Logger logger2 = logger;
                logger = null;
                Throwable throwable = ex;
                ex = null;
                ((IFn)const__4.getRawRoot()).invoke((Object)logger2, (Object)throwable);
            }
            object = ((IFn)const__0.getRawRoot()).invoke((Object)const__5, const__2);
        }
        return object;
    }
}


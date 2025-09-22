/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.process.CriticalFailure;
import datomic.process_monitor$fn__23521$fn__23522;
import datomic.process_monitor$fn__23521$fn__23524;
import org.slf4j.LoggerFactory;

public final class process_monitor$fn__23521
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Object const__10;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic() {
        block4: {
            block3: {
                v0 = temp__5457__auto__23527 = ((IFn)process_monitor$fn__23521.const__0.getRawRoot()).invoke();
                if (v0 == null || v0 == Boolean.FALSE) break block3;
                v1 = temp__5457__auto__23527;
                temp__5457__auto__23527 = null;
                callback = v1;
                logger = LoggerFactory.getLogger((String)"datomic.process-monitor");
                if (logger.isInfoEnabled()) {
                    v2 = logger;
                    logger = null;
                    v2.info((String)((IFn)process_monitor$fn__23521.const__1.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{process_monitor$fn__23521.const__2, ((IFn)process_monitor$fn__23521.const__3.getRawRoot()).invoke((Object)"datomic.metricsCallback")})));
                }
                if (Util.classOf((Object)(v3 = process_monitor$fn__23521.const__5.getRawRoot())) == process_monitor$fn__23521.__cached_class__0) ** GOTO lbl16
                if (!(v3 instanceof CriticalFailure)) {
                    v3 = v3;
                    process_monitor$fn__23521.__cached_class__0 = Util.classOf((Object)v3);
lbl16:
                    // 2 sources

                    v4 = process_monitor$fn__23521.const__4.getRawRoot().invoke(v3, (Object)new process_monitor$fn__23521$fn__23522(callback));
                } else {
                    v4 = ((CriticalFailure)v3).add_fail_handler((Object)new process_monitor$fn__23521$fn__23522(callback));
                }
                ((IFn)process_monitor$fn__23521.const__6.getRawRoot()).invoke((Object)new process_monitor$fn__23521$fn__23524(callback));
                v5 = callback;
                callback = null;
                v6 = ((IFn)process_monitor$fn__23521.const__7.getRawRoot()).invoke((Object)"Datomic Metrics Reporter", ((IFn)process_monitor$fn__23521.const__8.getRawRoot()).invoke(process_monitor$fn__23521.const__9.getRawRoot(), v5), process_monitor$fn__23521.const__10);
                break block4;
            }
            v6 = null;
        }
        return v6;
    }

    public Object invoke() {
        return process_monitor$fn__23521.invokeStatic();
    }

    static {
        const__0 = RT.var((String)"datomic.process-monitor", (String)"metrics-callback");
        const__1 = RT.var((String)"datomic.slf4j", (String)"process");
        const__2 = RT.keyword((String)"metrics", (String)"started");
        const__3 = RT.var((String)"datomic.config", (String)"property");
        const__4 = RT.var((String)"datomic.process", (String)"add-fail-handler");
        const__5 = RT.var((String)"datomic.process", (String)"instance");
        const__6 = RT.var((String)"clojure.core", (String)"future-call");
        const__7 = RT.var((String)"datomic.common", (String)"schedule");
        const__8 = RT.var((String)"clojure.core", (String)"partial");
        const__9 = RT.var((String)"datomic.process-monitor", (String)"report-metrics");
        const__10 = 60000L;
    }
}


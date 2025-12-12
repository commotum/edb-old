/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.process_monitor$convert_nanos_to_millis;
import datomic.process_monitor$fn__23484;
import datomic.process_monitor$fn__23486;
import datomic.process_monitor$fn__23521;
import datomic.process_monitor$loading__6434__auto____23482;
import datomic.process_monitor$metrics_callback;
import datomic.process_monitor$report_metrics;
import datomic.process_monitor$snapshot_metrics;
import datomic.process_monitor$start_metrics;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class process_monitor__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__6;
    public static final Var const__7;
    public static final AFn const__10;
    public static final Var const__11;
    public static final AFn const__13;
    public static final Var const__14;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__19;
    public static final Var const__20;
    public static final AFn const__22;
    public static final Var const__23;
    public static final AFn const__25;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new process_monitor$loading__6434__auto____23482()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new process_monitor$fn__23484())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__6);
        Var var2 = var;
        var.bindRoot((Object)new Delay((IFn)new process_monitor$fn__23486()));
        Var var3 = const__7;
        var3.setMeta((IPersistentMap)const__10);
        Var var4 = var3;
        var3.bindRoot((Object)new process_monitor$convert_nanos_to_millis());
        Var var5 = const__11;
        var5.setMeta((IPersistentMap)const__13);
        Var var6 = var5;
        var5.bindRoot((Object)new process_monitor$snapshot_metrics());
        Var var7 = const__14;
        var7.setMeta((IPersistentMap)const__16);
        Var var8 = var7;
        var7.bindRoot((Object)new process_monitor$metrics_callback());
        Var var9 = const__17;
        var9.setMeta((IPersistentMap)const__19);
        Var var10 = var9;
        var9.bindRoot((Object)new process_monitor$report_metrics());
        Var var11 = const__20;
        var11.setMeta((IPersistentMap)const__22);
        Var var12 = var11;
        var11.bindRoot((Object)new Delay((IFn)new process_monitor$fn__23521()));
        Var var13 = const__23;
        var13.setMeta((IPersistentMap)const__25);
        Var var14 = var13;
        var13.bindRoot((Object)new process_monitor$start_metrics());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.process-monitor");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.process-monitor", (String)"monitored-instances-ref");
        const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__7 = RT.var((String)"datomic.process-monitor", (String)"convert-nanos-to-millis");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"snapshot")))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.process-monitor", (String)"snapshot-metrics");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__14 = RT.var((String)"datomic.process-monitor", (String)"metrics-callback");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.process-monitor", (String)"report-metrics");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"callback")))), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.process-monitor", (String)"start-metrics-delay");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.process-monitor", (String)"start-metrics");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
    }

    static {
        process_monitor__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.process_monitor__init").getClassLoader());
        try {
            process_monitor__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


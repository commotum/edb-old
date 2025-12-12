/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.val_store.s3$fn__21349;
import datomic.core2.val_store.s3$go_with_metrics;
import datomic.core2.val_store.s3$loading__6789__auto____21347;
import datomic.core2.val_store.s3$retry_handler;
import datomic.core2.val_store.s3$storage_key;
import datomic.core2.val_store.s3$wrap_metric_handler;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class s3__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__11;
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__20;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__26;
    public static final AFn const__30;
    public static final Var const__31;
    public static final AFn const__32;
    public static final AFn const__36;
    public static final Var const__37;
    public static final AFn const__39;
    public static final Var const__40;
    public static final AFn const__42;
    public static final Var const__43;
    public static final AFn const__45;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new s3$loading__6789__auto____21347()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new s3$fn__21349())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new s3$storage_key());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)const__18);
        Var var5 = const__19;
        var5.setMeta((IPersistentMap)const__20);
        Var var6 = var5;
        var5.bindRoot((Object)const__24);
        Var var7 = const__25;
        var7.setMeta((IPersistentMap)const__26);
        Var var8 = var7;
        var7.bindRoot((Object)const__30);
        Var var9 = const__31;
        var9.setMeta((IPersistentMap)const__32);
        Var var10 = var9;
        var9.bindRoot((Object)const__36);
        Var var11 = const__37;
        var11.setMeta((IPersistentMap)const__39);
        Var var12 = var11;
        var11.bindRoot((Object)new s3$go_with_metrics());
        Var var13 = const__40;
        var13.setMeta((IPersistentMap)const__42);
        Var var14 = var13;
        var13.bindRoot((Object)new s3$wrap_metric_handler());
        Var var15 = const__43;
        var15.setMeta((IPersistentMap)const__45);
        Var var16 = var15;
        var15.bindRoot((Object)new s3$retry_handler());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.val-store.s3");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.core2.val-store.s3", (String)"storage-key");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"prefix"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.core2.val-store.s3", (String)"success-metrics");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"create"), RT.keyword(null, (String)"s3.put.succeeded.msec"), RT.keyword(null, (String)"get"), RT.keyword(null, (String)"s3.get.succeeded.msec"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"s3.delete.succeeded.msec")});
        const__19 = RT.var((String)"datomic.core2.val-store.s3", (String)"failure-metrics");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"create"), RT.keyword(null, (String)"s3.put.failed.msec"), RT.keyword(null, (String)"get"), RT.keyword(null, (String)"s3.get.failed.msec"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"s3.delete.failed.msec")});
        const__25 = RT.var((String)"datomic.core2.val-store.s3", (String)"retry-success-metrics");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"create"), RT.keyword(null, (String)"s3.put.retry.succeeded"), RT.keyword(null, (String)"get"), RT.keyword(null, (String)"s3.get.retry.succeeded"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"s3.delete.retry.succeeded")});
        const__31 = RT.var((String)"datomic.core2.val-store.s3", (String)"retry-failure-metrics");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"create"), RT.keyword(null, (String)"s3.put.retry.failed"), RT.keyword(null, (String)"get"), RT.keyword(null, (String)"s3.get.retry.failed"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"s3.delete.retry.failed")});
        const__37 = RT.var((String)"datomic.core2.val-store.s3", (String)"go-with-metrics");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op"), (Object)Symbol.intern(null, (String)"context")))), RT.keyword(null, (String)"column"), 1});
        const__40 = RT.var((String)"datomic.core2.val-store.s3", (String)"wrap-metric-handler");
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op"), (Object)Symbol.intern(null, (String)"context")))), RT.keyword(null, (String)"column"), 1});
        const__43 = RT.var((String)"datomic.core2.val-store.s3", (String)"retry-handler");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"op"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"backoff"), (Object)Symbol.intern(null, (String)"base"), (Object)Symbol.intern(null, (String)"retriable?")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"backoff"), 200L, Symbol.intern(null, (String)"base"), 2L, Symbol.intern(null, (String)"retriable?"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"partial"), Symbol.intern((String)"retry", (String)"limiting-retry"), 5L))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 45}))})})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        s3__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.val_store.s3__init").getClassLoader());
        try {
            s3__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


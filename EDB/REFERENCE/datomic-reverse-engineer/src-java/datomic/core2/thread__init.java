/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.LockingTransaction
 *  clojure.lang.MultiFn
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.thread$cached_thread_pool;
import datomic.core2.thread$clojure_name__GT_metric_name;
import datomic.core2.thread$daemon_factory;
import datomic.core2.thread$fixed_thread_pool;
import datomic.core2.thread$fn__21003;
import datomic.core2.thread$fn__21041;
import datomic.core2.thread$fn__21047;
import datomic.core2.thread$fn__21053;
import datomic.core2.thread$fn__21055;
import datomic.core2.thread$handoff_thread_pool;
import datomic.core2.thread$loading__6789__auto____21001;
import datomic.core2.thread$observable_thread_pool;
import datomic.core2.thread$pfuture;
import datomic.core2.thread$pfuture_ch;
import datomic.core2.thread$pmap_n;
import datomic.core2.thread$pthread;
import datomic.core2.thread$pthread_fn;
import datomic.core2.thread$result_chan;
import datomic.core2.thread$thread_pool;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class thread__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final AFn const__13;
    public static final Var const__14;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__19;
    public static final Var const__20;
    public static final AFn const__22;
    public static final Var const__23;
    public static final AFn const__25;
    public static final Var const__26;
    public static final AFn const__28;
    public static final Var const__29;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__37;
    public static final Var const__38;
    public static final Keyword const__39;
    public static final Object const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__52;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new thread$loading__6789__auto____21001()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new thread$fn__21003())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot(((IFn)const__8.getRawRoot()).invoke((Object)const__9));
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__13);
        Var var4 = var3;
        var3.bindRoot((Object)new thread$pthread_fn());
        Var var5 = const__14;
        var5.setMeta((IPersistentMap)const__16);
        Var var6 = var5;
        var5.bindRoot((Object)new thread$pthread());
        const__14.setMacro();
        Object v10 = null;
        Var var7 = const__14;
        Var var8 = const__17;
        var8.setMeta((IPersistentMap)const__19);
        Var var9 = var8;
        var8.bindRoot((Object)new thread$pfuture());
        Var var10 = const__20;
        var10.setMeta((IPersistentMap)const__22);
        Var var11 = var10;
        var10.bindRoot((Object)new thread$daemon_factory());
        Var var12 = const__23;
        var12.setMeta((IPersistentMap)const__25);
        Var var13 = var12;
        var12.bindRoot((Object)new thread$fixed_thread_pool());
        Var var14 = const__26;
        var14.setMeta((IPersistentMap)const__28);
        Var var15 = var14;
        var14.bindRoot((Object)new thread$result_chan());
        Var var16 = const__29;
        var16.setMeta((IPersistentMap)const__31);
        Var var17 = var16;
        var16.bindRoot((Object)new thread$pfuture_ch());
        Var var18 = const__32;
        var18.setMeta((IPersistentMap)const__34);
        Var var19 = var18;
        var18.bindRoot((Object)new thread$pmap_n());
        Object object4 = ((IFn)new thread$fn__21041()).invoke();
        Var var20 = const__35;
        var20.setMeta((IPersistentMap)const__37);
        Var var21 = var20;
        var20.bindRoot((Object)new thread$clojure_name__GT_metric_name());
        Object object5 = ((IFn)new thread$fn__21047()).invoke();
        MultiFn multiFn = ((MultiFn)const__38.getRawRoot()).addMethod((Object)const__39, (IFn)new thread$fn__21053());
        MultiFn multiFn2 = ((MultiFn)const__38.getRawRoot()).addMethod(const__40, (IFn)new thread$fn__21055());
        Var var22 = const__41;
        var22.setMeta((IPersistentMap)const__43);
        Var var23 = var22;
        var22.bindRoot((Object)new thread$observable_thread_pool());
        Var var24 = const__44;
        var24.setMeta((IPersistentMap)const__46);
        Var var25 = var24;
        var24.bindRoot((Object)new thread$thread_pool());
        Var var26 = const__47;
        var26.setMeta((IPersistentMap)const__49);
        Var var27 = var26;
        var26.bindRoot((Object)new thread$cached_thread_pool());
        Var var28 = const__50;
        var28.setMeta((IPersistentMap)const__52);
        Var var29 = var28;
        var28.bindRoot((Object)new thread$handoff_thread_pool());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.thread");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.core2.thread", (String)"binding-conveyor-fn");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__8 = RT.var((String)"clojure.core", (String)"deref");
        const__9 = RT.var((String)"clojure.core", (String)"binding-conveyor-fn");
        const__10 = RT.var((String)"datomic.core2.thread", (String)"pthread-fn");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"exec")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Executor")})), (Object)Symbol.intern(null, (String)"fn")))), RT.keyword(null, (String)"column"), 1});
        const__14 = RT.var((String)"datomic.core2.thread", (String)"pthread");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"exec"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.core2.thread", (String)"pfuture");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"f")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.Callable")})), (Object)((IObj)Symbol.intern(null, (String)"exec")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ExecutorService")}))))), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.core2.thread", (String)"daemon-factory");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"name-prefix"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.ThreadFactory")})), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"name-prefix"), (Object)((IObj)Symbol.intern(null, (String)"group")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ThreadGroup")})))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.ThreadFactory")})))), RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.core2.thread", (String)"fixed-thread-pool");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"name-prefix"), (Object)Symbol.intern(null, (String)"n"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.ExecutorService")})), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"name-prefix"), (Object)Symbol.intern(null, (String)"n"), (Object)Symbol.intern(null, (String)"group"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.ExecutorService")})))), RT.keyword(null, (String)"column"), 1});
        const__26 = RT.var((String)"datomic.core2.thread", (String)"result-chan");
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create(), Tuple.create((Object)Symbol.intern(null, (String)"n")), Tuple.create((Object)Symbol.intern(null, (String)"n"), (Object)Symbol.intern(null, (String)"xform")))), RT.keyword(null, (String)"column"), 1});
        const__29 = RT.var((String)"datomic.core2.thread", (String)"pfuture-ch");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"exec")), Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"exec"), (Object)Symbol.intern(null, (String)"ch")))), RT.keyword(null, (String)"column"), 1});
        const__32 = RT.var((String)"datomic.core2.thread", (String)"pmap-n");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"n"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"coll")), Tuple.create((Object)Symbol.intern(null, (String)"n"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"coll"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"colls")))), RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.core2.thread", (String)"clojure-name->metric-name");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__38 = RT.var((String)"datomic.core2.thread", (String)"cast-queue-metric");
        const__39 = RT.keyword(null, (String)"default");
        const__40 = RT.classForName((String)"java.util.concurrent.SynchronousQueue");
        const__41 = RT.var((String)"datomic.core2.thread", (String)"observable-thread-pool");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)((IObj)Symbol.intern(null, (String)"pool")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ThreadPoolExecutor")})), (Object)Symbol.intern(null, (String)"name"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.ExecutorService")})))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.core2.thread", (String)"thread-pool");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"name"), (Object)Symbol.intern(null, (String)"nthreads"), (Object)Symbol.intern(null, (String)"metrics?")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"metrics?"), Boolean.TRUE})}))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.ExecutorService")})))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.core2.thread", (String)"cached-thread-pool");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"name"), (Object)Symbol.intern(null, (String)"metrics?")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"metrics?"), Boolean.TRUE})}))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.ExecutorService")})))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.core2.thread", (String)"handoff-thread-pool");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"core-threads"), (Object)Symbol.intern(null, (String)"name"), (Object)Symbol.intern(null, (String)"max-threads"), (Object)Symbol.intern(null, (String)"metrics?")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"core-threads"), 2L, Symbol.intern(null, (String)"metrics?"), Boolean.TRUE})}))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.concurrent.ExecutorService")})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        thread__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.thread__init").getClassLoader());
        try {
            thread__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


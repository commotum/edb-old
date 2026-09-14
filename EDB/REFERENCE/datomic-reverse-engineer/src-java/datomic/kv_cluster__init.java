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
package datomic;

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
import datomic.kv_cluster$exponential_backoff;
import datomic.kv_cluster$fn__10851;
import datomic.kv_cluster$fn__10871;
import datomic.kv_cluster$fn__10877;
import datomic.kv_cluster$fn__10885;
import datomic.kv_cluster$kv_cluster;
import datomic.kv_cluster$linear_backoff;
import datomic.kv_cluster$loading__6434__auto____10373;
import datomic.kv_cluster$mark_pod_garbage;
import datomic.kv_cluster$notify_retry;
import datomic.kv_cluster$retry_cause;
import datomic.kv_cluster$retry_fn;
import datomic.kv_cluster$root_cause;
import datomic.kv_cluster$same_ref_QMARK_;
import datomic.kv_cluster$shared_pool;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class kv_cluster__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__15;
    public static final Var const__16;
    public static final AFn const__19;
    public static final Var const__20;
    public static final AFn const__22;
    public static final Var const__23;
    public static final AFn const__25;
    public static final Var const__26;
    public static final AFn const__28;
    public static final Var const__29;
    public static final AFn const__30;
    public static final Var const__31;
    public static final Object const__32;
    public static final Var const__33;
    public static final AFn const__34;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__41;
    public static final Var const__42;
    public static final AFn const__44;
    public static final Var const__45;
    public static final AFn const__47;
    public static final Object const__48;
    public static final Var const__49;
    public static final AFn const__51;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new kv_cluster$loading__6434__auto____10373()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new kv_cluster$fn__10851())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new kv_cluster$root_cause());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new kv_cluster$retry_cause());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new kv_cluster$notify_retry());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__19);
        Var var8 = var7;
        var7.bindRoot((Object)new kv_cluster$exponential_backoff());
        Var var9 = const__20;
        var9.setMeta((IPersistentMap)const__22);
        Var var10 = var9;
        var9.bindRoot((Object)new kv_cluster$linear_backoff());
        Var var11 = const__23;
        var11.setMeta((IPersistentMap)const__25);
        Var var12 = var11;
        var11.bindRoot((Object)new kv_cluster$retry_fn());
        Object object4 = ((IFn)new kv_cluster$fn__10871()).invoke();
        Var var13 = const__26;
        var13.setMeta((IPersistentMap)const__28);
        Var var14 = var13;
        var13.bindRoot((Object)new kv_cluster$shared_pool());
        Object object5 = ((IFn)new kv_cluster$fn__10877()).invoke();
        Var var15 = const__29;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot(((IFn)const__31.getRawRoot()).invoke(const__32));
        Var var17 = const__33;
        var17.setMeta((IPersistentMap)const__34);
        Var var18 = var17;
        var17.bindRoot((Object)const__38);
        Var var19 = const__39;
        var19.setMeta((IPersistentMap)const__41);
        Var var20 = var19;
        var19.bindRoot((Object)new kv_cluster$same_ref_QMARK_());
        Var var21 = const__42;
        var21.setMeta((IPersistentMap)const__44);
        Var var22 = var21;
        var21.bindRoot((Object)new kv_cluster$mark_pod_garbage());
        Object object6 = ((IFn)new kv_cluster$fn__10885()).invoke();
        Var var23 = const__45;
        var23.setMeta((IPersistentMap)const__47);
        Var var24 = var23;
        var23.bindRoot(const__48);
        Var var25 = const__49;
        var25.setMeta((IPersistentMap)const__51);
        Var var26 = var25;
        var25.bindRoot((Object)new kv_cluster$kv_cluster());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.kv-cluster");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.kv-cluster", (String)"root-cause");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.kv-cluster", (String)"retry-cause");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"result")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.kv-cluster", (String)"notify-retry");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"result"), (Object)Symbol.intern(null, (String)"metric"), (Object)Symbol.intern(null, (String)"backoff"), (Object)Symbol.intern(null, (String)"attempts"), (Object)Symbol.intern(null, (String)"max-retries")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.kv-cluster", (String)"exponential-backoff");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"n")))), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.kv-cluster", (String)"linear-backoff");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"n")))), RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.kv-cluster", (String)"retry-fn");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"sem")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Semaphore")})), (Object)Symbol.intern(null, (String)"metric"), (Object)Symbol.intern(null, (String)"nested"), (Object)Symbol.intern(null, (String)"group-ref"), (Object)Symbol.intern(null, (String)"backoff"), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__26 = RT.var((String)"datomic.kv-cluster", (String)"shared-pool");
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__29 = RT.var((String)"datomic.kv-cluster", (String)"val-gets-ref");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"clojure.core", (String)"atom");
        const__32 = 0L;
        const__33 = RT.var((String)"datomic.kv-cluster", (String)"ref-identity-keys");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__38 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"id"), (Object)RT.keyword(null, (String)"rev"), (Object)RT.keyword(null, (String)"key"));
        const__39 = RT.var((String)"datomic.kv-cluster", (String)"same-ref?");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"r1"), (Object)Symbol.intern(null, (String)"r2")))), RT.keyword(null, (String)"column"), 1});
        const__42 = RT.var((String)"datomic.kv-cluster", (String)"mark-pod-garbage");
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cs"), (Object)Symbol.intern(null, (String)"tail-keys-ref")))), RT.keyword(null, (String)"column"), 1});
        const__45 = RT.var((String)"datomic.kv-cluster", (String)"PRIORITY_WRITE_CONCURRENCY");
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__48 = 2L;
        const__49 = RT.var((String)"datomic.kv-cluster", (String)"kv-cluster");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"kvs"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"tenant"), Symbol.intern(null, (String)"db-id"), Symbol.intern(null, (String)"write-concurrency"), Symbol.intern(null, (String)"read-concurrency"), Symbol.intern(null, (String)"shared-pool?"), Symbol.intern(null, (String)"protocol"), Symbol.intern(null, (String)"pod-garbage-handler"), Symbol.intern(null, (String)"retrying-delete")}), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"shared-pool?"), Boolean.TRUE, Symbol.intern(null, (String)"protocol"), RT.keyword(null, (String)"kvc"), Symbol.intern(null, (String)"pod-garbage-handler"), Symbol.intern(null, (String)"mark-pod-garbage")}), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"x")})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        kv_cluster__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.kv_cluster__init").getClassLoader());
        try {
            kv_cluster__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


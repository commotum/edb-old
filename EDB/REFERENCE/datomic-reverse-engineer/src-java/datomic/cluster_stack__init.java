/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
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
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.cluster_stack$cluster_with_cache;
import datomic.cluster_stack$fn__11252;
import datomic.cluster_stack$fn__11254;
import datomic.cluster_stack$fn__11259;
import datomic.cluster_stack$fn__11272;
import datomic.cluster_stack$fn__11436;
import datomic.cluster_stack$fn__11443;
import datomic.cluster_stack$loading__6434__auto____11158;
import datomic.cluster_stack$result__GT_anom;
import datomic.cluster_stack$start_kv_cache;
import datomic.cluster_stack$val_store_on_cluster;
import datomic.cluster_stack$val_store_on_kv_cache;
import datomic.cluster_stack$val_store_with_close;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class cluster_stack__init {
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
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__23;
    public static final Var const__24;
    public static final AFn const__26;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new cluster_stack$loading__6434__auto____11158()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new cluster_stack$fn__11252())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new cluster_stack$fn__11254()).invoke();
        Object object5 = ((IFn)new cluster_stack$fn__11259()).invoke();
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new cluster_stack$val_store_on_kv_cache());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new cluster_stack$result__GT_anom());
        Object object6 = ((IFn)new cluster_stack$fn__11272()).invoke();
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new cluster_stack$val_store_on_cluster());
        Object object7 = ((IFn)new cluster_stack$fn__11436()).invoke();
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new cluster_stack$val_store_with_close());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot(((IFn)new cluster_stack$fn__11443()).invoke());
        Var var11 = const__21;
        var11.setMeta((IPersistentMap)const__23);
        Var var12 = var11;
        var11.bindRoot((Object)new cluster_stack$start_kv_cache());
        Var var13 = const__24;
        var13.setMeta((IPersistentMap)const__26);
        Var var14 = var13;
        var13.bindRoot((Object)new cluster_stack$cluster_with_cache());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cluster-stack");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.cluster-stack", (String)"val-store-on-kv-cache");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"exec"), (Object)Symbol.intern(null, (String)"kv-cache")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.cluster-stack", (String)"result->anom");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"result")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.cluster-stack", (String)"val-store-on-cluster");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.cluster-stack", (String)"val-store-with-close");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"store"), (Object)Symbol.intern(null, (String)"close")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.cluster-stack", (String)"kv-cache-ref");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.cluster-stack", (String)"start-kv-cache");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__24 = RT.var((String)"datomic.cluster-stack", (String)"cluster-with-cache");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"cache")), Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"cache"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        cluster_stack__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cluster_stack__init").getClassLoader());
        try {
            cluster_stack__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


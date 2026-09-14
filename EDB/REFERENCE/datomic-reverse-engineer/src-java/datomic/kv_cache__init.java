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
import datomic.kv_cache$fn__10067;
import datomic.kv_cache$fn__9933;
import datomic.kv_cache$get_kv_cache_ref;
import datomic.kv_cache$loading__6434__auto____9931;
import datomic.kv_cache$shutdown;
import datomic.kv_cache$start_kv_cache;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class kv_cache__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__5;
    public static final AFn const__6;
    public static final Var const__7;
    public static final AFn const__11;
    public static final Var const__12;
    public static final AFn const__15;
    public static final Var const__16;
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__21;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new kv_cache$loading__6434__auto____9931()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new kv_cache$fn__9933())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, (Object)const__6);
        Var var = const__7;
        var.setMeta((IPersistentMap)const__11);
        Var var2 = var;
        var.bindRoot(((IFn)new kv_cache$fn__10067()).invoke());
        Var var3 = const__12;
        var3.setMeta((IPersistentMap)const__15);
        Var var4 = var3;
        var3.bindRoot((Object)new kv_cache$shutdown());
        Var var5 = const__16;
        var5.setMeta((IPersistentMap)const__18);
        Var var6 = var5;
        var5.bindRoot((Object)new kv_cache$get_kv_cache_ref());
        Var var7 = const__19;
        var7.setMeta((IPersistentMap)const__21);
        Var var8 = var7;
        var7.bindRoot((Object)new kv_cache$start_kv_cache());
        Object v13 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.kv-cache");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.require", (String)"maybe-require");
        const__5 = (AFn)Symbol.intern(null, (String)"datomic.memcached");
        const__6 = (AFn)Symbol.intern(null, (String)"datomic.valcache-direct");
        const__7 = RT.var((String)"datomic.kv-cache", (String)"kv-cache-ref");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.kv-cache", (String)"shutdown");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.kv-cache", (String)"get-kv-cache-ref");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.kv-cache", (String)"start-kv-cache");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
    }

    static {
        kv_cache__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.kv_cache__init").getClassLoader());
        try {
            kv_cache__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


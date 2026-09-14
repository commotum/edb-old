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
import datomic.kv_dynamo_skv$expected_map;
import datomic.kv_dynamo_skv$fn__23531;
import datomic.kv_dynamo_skv$fn__23541;
import datomic.kv_dynamo_skv$kv_ddb_skv_;
import datomic.kv_dynamo_skv$loading__6434__auto____23529;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class kv_dynamo_skv__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__14;
    public static final Var const__15;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new kv_dynamo_skv$loading__6434__auto____23529()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new kv_dynamo_skv$fn__23531())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new kv_dynamo_skv$expected_map());
        Object object4 = ((IFn)new kv_dynamo_skv$fn__23541()).invoke();
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new kv_dynamo_skv$kv_ddb_skv_());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot(((IFn)const__15.getRawRoot()).invoke(const__10.getRawRoot()));
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.kv-dynamo-skv");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.kv-dynamo-skv", (String)"expected-map");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"expect-map")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.kv-dynamo-skv", (String)"kv-ddb-skv-");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"creds"), (Object)((IObj)Symbol.intern(null, (String)"prefix")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)Symbol.intern(null, (String)"skv")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.kv-dynamo-skv", (String)"kv-ddb-skv");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"clojure.core", (String)"memoize");
    }

    static {
        kv_dynamo_skv__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.kv_dynamo_skv__init").getClassLoader());
        try {
            kv_dynamo_skv__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


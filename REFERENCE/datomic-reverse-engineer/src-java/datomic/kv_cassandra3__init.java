/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.PersistentArrayMap
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
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.kv_cassandra3$fn__23642;
import datomic.kv_cassandra3$fn__23645;
import datomic.kv_cassandra3$kv_cassandra;
import datomic.kv_cassandra3$loading__6434__auto____23640;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class kv_cassandra3__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final AFn const__10;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__20;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new kv_cassandra3$loading__6434__auto____23640()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new kv_cassandra3$fn__23642())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot(((IFn)const__8.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY));
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__10);
        Var var4 = var3;
        var3.bindRoot((Object)const__16);
        Object object4 = ((IFn)new kv_cassandra3$fn__23645()).invoke();
        Var var5 = const__17;
        var5.setMeta((IPersistentMap)const__20);
        Var var6 = var5;
        var5.bindRoot((Object)new kv_cassandra3$kv_cassandra());
        Object v11 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.kv-cassandra3");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.kv-cassandra3", (String)"sessions");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__8 = RT.var((String)"clojure.core", (String)"atom");
        const__9 = RT.var((String)"datomic.kv-cassandra3", (String)"cql-keys");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__16 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"id2"), (Object)RT.keyword(null, (String)"rev"), (Object)RT.keyword(null, (String)"map"), (Object)RT.keyword(null, (String)"val"), (Object)RT.keyword(null, (String)"chunks"));
        const__17 = RT.var((String)"datomic.kv-cassandra3", (String)"kv-cassandra");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"endpoint")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        kv_cassandra3__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.kv_cassandra3__init").getClassLoader());
        try {
            kv_cassandra3__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


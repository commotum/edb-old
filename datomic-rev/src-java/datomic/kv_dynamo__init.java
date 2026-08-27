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
import datomic.kv_dynamo$expected_map;
import datomic.kv_dynamo$fn__20476;
import datomic.kv_dynamo$fn__20487;
import datomic.kv_dynamo$key_path;
import datomic.kv_dynamo$kv_dynamo;
import datomic.kv_dynamo$loading__6434__auto____20474;
import datomic.kv_dynamo$remove_prefix;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class kv_dynamo__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__15;
    public static final Var const__16;
    public static final AFn const__18;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new kv_dynamo$loading__6434__auto____20474()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new kv_dynamo$fn__20476())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new kv_dynamo$key_path());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new kv_dynamo$remove_prefix());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new kv_dynamo$expected_map());
        Object object3 = ((IFn)new kv_dynamo$fn__20487()).invoke();
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new kv_dynamo$kv_dynamo());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.kv-dynamo");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.kv-dynamo", (String)"key-path");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"prefix"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.kv-dynamo", (String)"remove-prefix");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"prefix"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.kv-dynamo", (String)"expected-map");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"expect-map")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.kv-dynamo", (String)"kv-dynamo");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"table")), Tuple.create((Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"prefix")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        kv_dynamo__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.kv_dynamo__init").getClassLoader());
        try {
            kv_dynamo__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


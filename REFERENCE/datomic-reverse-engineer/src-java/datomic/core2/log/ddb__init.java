/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AReference
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2.log;

import clojure.lang.AFn;
import clojure.lang.AReference;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.log.ddb$append_request;
import datomic.core2.log.ddb$create;
import datomic.core2.log.ddb$ddb_item__GT_log_item;
import datomic.core2.log.ddb$fn__20636;
import datomic.core2.log.ddb$fn__20642;
import datomic.core2.log.ddb$loading__6789__auto____20634;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ddb__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Var const__5;
    public static final AFn const__11;
    public static final Var const__12;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__17;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__3);
        Object object2 = ((IFn)new ddb$loading__6789__auto____20634()).invoke();
        if (((Symbol)const__1).equals((Object)const__4)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new ddb$fn__20636())));
            v3 = null;
        }
        Var var = const__5;
        var.setMeta((IPersistentMap)const__11);
        Var var2 = var;
        var.bindRoot((Object)new ddb$append_request());
        Var var3 = const__12;
        var3.setMeta((IPersistentMap)const__14);
        Var var4 = var3;
        var3.bindRoot((Object)new ddb$ddb_item__GT_log_item());
        Object object3 = ((IFn)new ddb$fn__20642()).invoke();
        Var var5 = const__15;
        var5.setMeta((IPersistentMap)const__17);
        Var var6 = var5;
        var5.bindRoot((Object)new ddb$create());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.core2.log.ddb")).withMeta(RT.map((Object[])new Object[0]));
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "Implementaion of datomic.core2.log backed by DynamoDB."});
        const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__5 = RT.var((String)"datomic.core2.log.ddb", (String)"append-request");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"p"), (Object)Symbol.intern(null, (String)"header"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.core2.log.ddb", (String)"ddb-item->log-item");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ddb-item")))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.core2.log.ddb", (String)"create");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"p"), (Object)Symbol.intern(null, (String)"chunk-size"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        ddb__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.log.ddb__init").getClassLoader());
        try {
            ddb__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


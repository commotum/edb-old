/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
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
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.val_cluster$fn__11184;
import datomic.val_cluster$fn__11188;
import datomic.val_cluster$fn__11191;
import datomic.val_cluster$fn__11206;
import datomic.val_cluster$loading__6434__auto____11182;
import datomic.val_cluster$val_cluster;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class val_cluster__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Object const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final ISeq const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__16;
    public static final Keyword const__17;
    public static final AFn const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final AFn const__22;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final AFn const__27;
    public static final AFn const__28;
    public static final Keyword const__29;
    public static final Var const__30;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__37;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new val_cluster$loading__6434__auto____11182()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new val_cluster$fn__11184())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new val_cluster$fn__11188()).invoke();
        Object object5 = const__4;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, const__7.getRawRoot(), (Object)const__8, null);
        Object object7 = ((IFn)const__9).invoke((Object)const__6, (Object)const__10);
        Object object8 = ((IFn)const__11.getRawRoot()).invoke((Object)const__6, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__6, (Object)const__20, (Object)const__22, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__27, ((IFn)const__12.getRawRoot()).invoke((Object)const__28, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__6})))), new val_cluster$fn__11191()})));
        Object object9 = ((IFn)const__30.getRawRoot()).invoke(const__6.getRawRoot());
        AFn aFn = const__31;
        Object object10 = ((IFn)new val_cluster$fn__11206()).invoke();
        Var var = const__32;
        var.setMeta((IPersistentMap)const__37);
        Var var2 = var;
        var.bindRoot((Object)new val_cluster$val_cluster());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.val-cluster");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.classForName((String)"datomic.val_cluster.Impl");
        const__5 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__6 = RT.var((String)"datomic.val-cluster", (String)"Impl");
        const__7 = RT.var((String)"clojure.core", (String)"assoc");
        const__8 = RT.keyword(null, (String)"doc");
        const__9 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__10 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-key"), (Object)Symbol.intern(null, (String)"opts"))))}))));
        const__11 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.val_cluster.Impl"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.val_cluster.Impl")});
        const__17 = RT.keyword(null, (String)"sigs");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-get"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-key"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-key"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Impl of cluster/ClusteredStore that takes nilable opts map."})});
        const__19 = RT.keyword(null, (String)"var");
        const__20 = RT.keyword(null, (String)"method-map");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-get"), RT.keyword(null, (String)"-get")});
        const__23 = RT.keyword(null, (String)"method-builders");
        const__24 = RT.var((String)"clojure.core", (String)"intern");
        const__25 = RT.var((String)"clojure.core", (String)"*ns*");
        const__26 = RT.var((String)"clojure.core", (String)"with-meta");
        const__27 = (AFn)((IObj)Symbol.intern(null, (String)"-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-key"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-key"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-key"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Impl of cluster/ClusteredStore that takes nilable opts map."});
        const__29 = RT.keyword(null, (String)"protocol");
        const__30 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__31 = (AFn)Symbol.intern(null, (String)"Impl");
        const__32 = RT.var((String)"datomic.val-cluster", (String)"val-cluster");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"val-store")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        val_cluster__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.val_cluster__init").getClassLoader());
        try {
            val_cluster__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


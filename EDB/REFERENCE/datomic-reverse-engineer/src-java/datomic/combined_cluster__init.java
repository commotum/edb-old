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
package datomic;

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
import datomic.combined_cluster$combined_cluster;
import datomic.combined_cluster$fn__11174;
import datomic.combined_cluster$fn__11176;
import datomic.combined_cluster$loading__6434__auto____11172;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class combined_cluster__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final AFn const__11;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__3);
        Object object2 = ((IFn)new combined_cluster$loading__6434__auto____11172()).invoke();
        if (((Symbol)const__1).equals((Object)const__4)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new combined_cluster$fn__11174())));
            v3 = null;
        }
        Object object3 = const__5.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new combined_cluster$fn__11176()).invoke();
        Var var = const__6;
        var.setMeta((IPersistentMap)const__11);
        Var var2 = var;
        var.bindRoot((Object)new combined_cluster$combined_cluster());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.combined-cluster")).withMeta(RT.map((Object[])new Object[0]));
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "A combiner for ClusteredStores.\nTakes two ClusteredStores, one for refs and one for values."});
        const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__5 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__6 = RT.var((String)"datomic.combined-cluster", (String)"combined-cluster");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ref-cluster"), (Object)Symbol.intern(null, (String)"val-cluster")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        combined_cluster__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.combined_cluster__init").getClassLoader());
        try {
            combined_cluster__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


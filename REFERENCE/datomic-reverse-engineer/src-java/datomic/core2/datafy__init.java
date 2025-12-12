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
package datomic.core2;

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
import datomic.core2.datafy$_datafy_declared_fields_fn;
import datomic.core2.datafy$datafy_declared_fields;
import datomic.core2.datafy$fn__20543;
import datomic.core2.datafy$loading__6789__auto____20541;
import datomic.core2.datafy$val_navs;
import datomic.core2.datafy$with_nav;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class datafy__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__8;
    public static final Var const__9;
    public static final AFn const__11;
    public static final Var const__12;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__17;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new datafy$loading__6789__auto____20541()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new datafy$fn__20543())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)new datafy$_datafy_declared_fields_fn());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new datafy$datafy_declared_fields());
        const__9.setMacro();
        Object v7 = null;
        Var var5 = const__9;
        Var var6 = const__12;
        var6.setMeta((IPersistentMap)const__14);
        Var var7 = var6;
        var6.bindRoot((Object)new datafy$val_navs());
        Var var8 = const__15;
        var8.setMeta((IPersistentMap)const__17);
        Var var9 = var8;
        var8.bindRoot((Object)new datafy$with_nav());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.datafy");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.core2.datafy", (String)"-datafy-declared-fields-fn");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"clsym")))), RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.core2.datafy", (String)"datafy-declared-fields");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"classes")))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.core2.datafy", (String)"val-navs");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"k->f")))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.core2.datafy", (String)"with-nav");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x"), (Object)Symbol.intern(null, (String)"nav-fn")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        datafy__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.datafy__init").getClassLoader());
        try {
            datafy__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


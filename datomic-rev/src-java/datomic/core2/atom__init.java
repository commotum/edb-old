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
package datomic.core2;

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
import datomic.core2.atom$fn__19660;
import datomic.core2.atom$loading__6789__auto____19658;
import datomic.core2.atom$reset_BANG_;
import datomic.core2.atom$swap_BANG_;
import datomic.core2.atom$swap_vals_BANG_;
import datomic.core2.atom$sync;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class atom__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Var const__5;
    public static final AFn const__10;
    public static final Var const__11;
    public static final AFn const__13;
    public static final Var const__14;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__19;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__3);
        Object object2 = ((IFn)new atom$loading__6789__auto____19658()).invoke();
        if (((Symbol)const__1).equals((Object)const__4)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new atom$fn__19660())));
            v3 = null;
        }
        Var var = const__5;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)new atom$reset_BANG_());
        Var var3 = const__11;
        var3.setMeta((IPersistentMap)const__13);
        Var var4 = var3;
        var3.bindRoot((Object)new atom$swap_vals_BANG_());
        Var var5 = const__14;
        var5.setMeta((IPersistentMap)const__16);
        Var var6 = var5;
        var5.bindRoot((Object)new atom$swap_BANG_());
        Var var7 = const__17;
        var7.setMeta((IPersistentMap)const__19);
        Var var8 = var7;
        var7.bindRoot((Object)new atom$sync());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.core2.atom")).withMeta(RT.map((Object[])new Object[0]));
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "API for a durable atom-like reference type with an\nin-memory copy of the latest known value.\n\nDurable atoms support reset!/swap-vals!/swap!, similar to in-memory\natoms but returning a channel. They also implement clojure.lang.IRef.\n\natom.logged provides a reference implementation of durable atoms\nbacked by a datomic.core2.log.\n\nDurable atom providers must implement spi/DurableAtom and\nclojure.lang.IRef."});
        const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__5 = RT.var((String)"datomic.core2.atom", (String)"reset!");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.core2.atom", (String)"swap-vals!");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
        const__14 = RT.var((String)"datomic.core2.atom", (String)"swap!");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.core2.atom", (String)"sync");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"a")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        atom__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.atom__init").getClassLoader());
        try {
            atom__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


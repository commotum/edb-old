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
package datomic.core2;

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
import datomic.core2.async$_LT__BANG__BANG_x;
import datomic.core2.async$_LT__BANG_x;
import datomic.core2.async$aderef;
import datomic.core2.async$aderef_n;
import datomic.core2.async$channel_closed_error;
import datomic.core2.async$fn__19468;
import datomic.core2.async$loading__6789__auto____19466;
import datomic.core2.async$put_all_BANG_;
import datomic.core2.async$retry;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class async__init {
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
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__23;
    public static final Var const__24;
    public static final AFn const__26;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new async$loading__6789__auto____19466()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new async$fn__19468())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)new async$aderef());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new async$aderef_n());
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot((Object)new async$retry());
        Var var7 = const__15;
        var7.setMeta((IPersistentMap)const__17);
        Var var8 = var7;
        var7.bindRoot((Object)new async$put_all_BANG_());
        Var var9 = const__18;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot((Object)new async$channel_closed_error());
        Var var11 = const__21;
        var11.setMeta((IPersistentMap)const__23);
        Var var12 = var11;
        var11.bindRoot((Object)new async$_LT__BANG_x());
        const__21.setMacro();
        Object v15 = null;
        Var var13 = const__21;
        Var var14 = const__24;
        var14.setMeta((IPersistentMap)const__26);
        Var var15 = var14;
        var14.bindRoot((Object)new async$_LT__BANG__BANG_x());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.async");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.core2.async", (String)"aderef");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ch")), Tuple.create((Object)Symbol.intern(null, (String)"ch"), (Object)Symbol.intern(null, (String)"timeout-ms")), Tuple.create((Object)Symbol.intern(null, (String)"ch"), (Object)Symbol.intern(null, (String)"timeout-ms"), (Object)Symbol.intern(null, (String)"timeout-val")))), RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.core2.async", (String)"aderef-n");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"n"), (Object)Symbol.intern(null, (String)"ch")), Tuple.create((Object)Symbol.intern(null, (String)"n"), (Object)Symbol.intern(null, (String)"ch"), (Object)Symbol.intern(null, (String)"timeout-ms")), Tuple.create((Object)Symbol.intern(null, (String)"n"), (Object)Symbol.intern(null, (String)"ch"), (Object)Symbol.intern(null, (String)"timeout-ms"), (Object)Symbol.intern(null, (String)"timeout-val")))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.core2.async", (String)"retry");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"pred"), (Object)Symbol.intern(null, (String)"backoff"), (Object)Symbol.intern(null, (String)"ch"), (Object)Symbol.intern(null, (String)"fail")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"ch"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern((String)"a", (String)"chan"), 1L))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 15})), Symbol.intern(null, (String)"fail"), Symbol.intern(null, (String)"identity")})})))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.core2.async", (String)"put-all!");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ch"), (Object)Symbol.intern(null, (String)"coll")), Tuple.create((Object)Symbol.intern(null, (String)"ch"), (Object)Symbol.intern(null, (String)"coll"), (Object)Symbol.intern(null, (String)"close?")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.core2.async", (String)"channel-closed-error");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.core2.async", (String)"<!x");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ch")))), RT.keyword(null, (String)"column"), 1});
        const__24 = RT.var((String)"datomic.core2.async", (String)"<!!x");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ch")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        async__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.async__init").getClassLoader());
        try {
            async__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


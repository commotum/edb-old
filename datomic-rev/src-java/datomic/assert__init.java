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
import datomic.assert$assert;
import datomic.assert$assertion_repl;
import datomic.assert$fn__20647;
import datomic.assert$loading__6434__auto____20645;
import datomic.assert$local_bindings;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class assert__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__7;
    public static final Object const__8;
    public static final Var const__9;
    public static final AFn const__10;
    public static final Var const__11;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__23;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new assert$loading__6434__auto____20645()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new assert$fn__20647())));
            v2 = null;
        }
        Var var = const__3.setDynamic(true);
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot(const__8);
        Var var3 = const__9.setDynamic(true);
        var3.setMeta((IPersistentMap)const__10);
        Var var4 = var3;
        var3.bindRoot(const__8);
        Var var5 = const__11.setDynamic(true);
        var5.setMeta((IPersistentMap)const__12);
        Var var6 = var5;
        var5.bindRoot(null);
        Var var7 = const__13;
        var7.setMeta((IPersistentMap)const__17);
        Var var8 = var7;
        var7.bindRoot((Object)new assert$local_bindings());
        Var var9 = const__18;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot((Object)new assert$assertion_repl());
        Var var11 = const__21;
        var11.setMeta((IPersistentMap)const__23);
        Var var12 = var11;
        var11.bindRoot((Object)new assert$assert());
        const__21.setMacro();
        Object v15 = null;
        Var var13 = const__21;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.assert");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.assert", (String)"*level*");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"dynamic"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__8 = 0L;
        const__9 = RT.var((String)"datomic.assert", (String)"*result*");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"dynamic"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.assert", (String)"*assert-handler*");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"dynamic"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.assert", (String)"local-bindings");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"env")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.assert", (String)"assertion-repl");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"error")))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.assert", (String)"assert");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")), Tuple.create((Object)Symbol.intern(null, (String)"x"), (Object)Symbol.intern(null, (String)"msg")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        assert__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.assert__init").getClassLoader());
        try {
            assert__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


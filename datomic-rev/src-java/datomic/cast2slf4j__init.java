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
import datomic.cast2slf4j$fn__21286;
import datomic.cast2slf4j$fn__21288;
import datomic.cast2slf4j$fn__21290;
import datomic.cast2slf4j$loading__6434__auto____21284;
import datomic.cast2slf4j$redirect;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class cast2slf4j__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final AFn const__11;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new cast2slf4j$loading__6434__auto____21284()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new cast2slf4j$fn__21286())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__6);
        Var var2 = var;
        var.bindRoot(((IFn)const__7.getRawRoot()).invoke((Object)new cast2slf4j$fn__21288()));
        Object object3 = ((IFn)new cast2slf4j$fn__21290()).invoke();
        Var var3 = const__8;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new cast2slf4j$redirect());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cast2slf4j");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.cast2slf4j", (String)"cast-name->cloudwatch-name");
        const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__7 = RT.var((String)"clojure.core", (String)"memoize");
        const__8 = RT.var((String)"datomic.cast2slf4j", (String)"redirect");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
    }

    static {
        cast2slf4j__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cast2slf4j__init").getClassLoader());
        try {
            cast2slf4j__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


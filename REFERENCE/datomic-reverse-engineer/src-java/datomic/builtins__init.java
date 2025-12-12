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
import datomic.builtins$build_retract_args;
import datomic.builtins$compare_and_swap;
import datomic.builtins$component_attr_QMARK_;
import datomic.builtins$component_es_set;
import datomic.builtins$fn__23381;
import datomic.builtins$loading__6434__auto____23379;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class builtins__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
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
        Object object2 = ((IFn)new builtins$loading__6434__auto____23379()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new builtins$fn__23381())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new builtins$component_attr_QMARK_());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new builtins$component_es_set());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new builtins$build_retract_args());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new builtins$compare_and_swap());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.builtins");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.builtins", (String)"component-attr?");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"a")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.builtins", (String)"component-es-set");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e")), Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"via-attrs")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.builtins", (String)"build-retract-args");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.builtins", (String)"compare-and-swap");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"v-old"), (Object)Symbol.intern(null, (String)"v-new")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        builtins__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.builtins__init").getClassLoader());
        try {
            builtins__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


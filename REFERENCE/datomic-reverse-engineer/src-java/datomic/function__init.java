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
 *  clojure.lang.MultiFn
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
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.function$compile_clojure;
import datomic.function$construct;
import datomic.function$fn__11932;
import datomic.function$fn__11935;
import datomic.function$fn__11971;
import datomic.function$fn__11973;
import datomic.function$loading__6434__auto____11924;
import datomic.function$normalize;
import datomic.function$print_function;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class function__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__10;
    public static final Var const__11;
    public static final Object const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__19;
    public static final Var const__20;
    public static final AFn const__22;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new function$loading__6434__auto____11924()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new function$fn__11932())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new function$fn__11935()).invoke();
        Var var = const__4;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)new function$print_function());
        MultiFn multiFn = ((MultiFn)const__11.getRawRoot()).addMethod(const__12, (IFn)new function$fn__11971());
        MultiFn multiFn2 = ((MultiFn)const__13.getRawRoot()).addMethod(const__12, (IFn)new function$fn__11973());
        Var var3 = const__14;
        var3.setMeta((IPersistentMap)const__16);
        Var var4 = var3;
        var3.bindRoot((Object)new function$compile_clojure());
        Var var5 = const__17;
        var5.setMeta((IPersistentMap)const__19);
        Var var6 = var5;
        var5.bindRoot((Object)new function$normalize());
        Var var7 = const__20;
        var7.setMeta((IPersistentMap)const__22);
        Var var8 = var7;
        var7.bindRoot((Object)new function$construct());
        Object v15 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.function");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.function", (String)"print-function");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"dbfn"), (Object)((IObj)Symbol.intern(null, (String)"w")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.io.Writer")}))))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"clojure.core", (String)"print-method");
        const__12 = RT.classForName((String)"datomic.function.Function");
        const__13 = RT.var((String)"clojure.core", (String)"print-dup");
        const__14 = RT.var((String)"datomic.function", (String)"compile-clojure");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"imports"), (Object)Symbol.intern(null, (String)"requires"), (Object)Symbol.intern(null, (String)"params"), (Object)Symbol.intern(null, (String)"code")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.function", (String)"normalize");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.function", (String)"construct");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        function__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.function__init").getClassLoader());
        try {
            function__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


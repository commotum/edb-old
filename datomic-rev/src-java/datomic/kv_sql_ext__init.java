/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
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
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.kv_sql_ext$cluster_conf__GT_spec;
import datomic.kv_sql_ext$fn__11539;
import datomic.kv_sql_ext$fn__11542;
import datomic.kv_sql_ext$fn__11546;
import datomic.kv_sql_ext$fn__11548;
import datomic.kv_sql_ext$fn__11557;
import datomic.kv_sql_ext$kv_sql;
import datomic.kv_sql_ext$loading__6434__auto____11537;
import datomic.kv_sql_ext$provider;
import datomic.kv_sql_ext$try_validation_query;
import datomic.kv_sql_ext$validation_query;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class kv_sql_ext__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__8;
    public static final Var const__9;
    public static final AFn const__12;
    public static final Var const__13;
    public static final Keyword const__14;
    public static final Var const__15;
    public static final AFn const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new kv_sql_ext$loading__6434__auto____11537()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new kv_sql_ext$fn__11539())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot(new Object());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new kv_sql_ext$provider());
        Object object4 = ((IFn)new kv_sql_ext$fn__11542()).invoke();
        MultiFn multiFn = ((MultiFn)const__13.getRawRoot()).addMethod((Object)const__14, (IFn)new kv_sql_ext$fn__11546());
        MultiFn multiFn2 = ((MultiFn)const__13.getRawRoot()).addMethod((Object)"oracle", (IFn)new kv_sql_ext$fn__11548());
        Var var5 = const__15;
        var5.setMeta((IPersistentMap)const__17);
        Var var6 = var5;
        var5.bindRoot((Object)new kv_sql_ext$validation_query());
        Var var7 = const__18;
        var7.setMeta((IPersistentMap)const__20);
        Var var8 = var7;
        var7.bindRoot((Object)new kv_sql_ext$try_validation_query());
        Var var9 = const__21;
        var9.setMeta((IPersistentMap)const__22);
        Var var10 = var9;
        var9.bindRoot(((IFn)const__23.getRawRoot()).invoke((Object)new kv_sql_ext$fn__11557()));
        Var var11 = const__24;
        var11.setMeta((IPersistentMap)const__26);
        Var var12 = var11;
        var11.bindRoot((Object)new kv_sql_ext$cluster_conf__GT_spec());
        Var var13 = const__27;
        var13.setMeta((IPersistentMap)const__29);
        Var var14 = var13;
        var13.bindRoot((Object)new kv_sql_ext$kv_sql());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.kv-sql-ext");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.kv-sql-ext", (String)"driver-manager-lock");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.kv-sql-ext", (String)"provider");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"url")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.kv-sql-ext", (String)"validation-query*");
        const__14 = RT.keyword(null, (String)"default");
        const__15 = RT.var((String)"datomic.kv-sql-ext", (String)"validation-query");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"provider")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.kv-sql-ext", (String)"try-validation-query");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sql-url"), (Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.kv-sql-ext", (String)"create-datasource");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"clojure.core", (String)"memoize");
        const__24 = RT.var((String)"datomic.kv-sql-ext", (String)"cluster-conf->spec");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"sql-url"), (Object)Symbol.intern(null, (String)"data-source"), (Object)Symbol.intern(null, (String)"factory")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cluster-conf")})))), RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.kv-sql-ext", (String)"kv-sql");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        kv_sql_ext__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.kv_sql_ext__init").getClassLoader());
        try {
            kv_sql_ext__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


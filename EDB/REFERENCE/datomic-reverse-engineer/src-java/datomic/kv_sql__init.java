/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
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
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.kv_sql$constraint_violation_QMARK_;
import datomic.kv_sql$fn__11535;
import datomic.kv_sql$fn__11575;
import datomic.kv_sql$fn__11587;
import datomic.kv_sql$from_spec;
import datomic.kv_sql$loading__6434__auto____11533;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class kv_sql__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__5;
    public static final Var const__6;
    public static final AFn const__11;
    public static final Var const__12;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__17;
    public static final Object const__18;
    public static final Var const__19;
    public static final Object const__20;
    public static final Var const__21;
    public static final Keyword const__22;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new kv_sql$loading__6434__auto____11533()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new kv_sql$fn__11535())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5);
        Var var = const__6;
        var.setMeta((IPersistentMap)const__11);
        Var var2 = var;
        var.bindRoot((Object)new kv_sql$constraint_violation_QMARK_());
        Object object5 = ((IFn)new kv_sql$fn__11575()).invoke();
        Var var3 = const__12;
        var3.setMeta((IPersistentMap)const__14);
        Var var4 = var3;
        var3.bindRoot((Object)new kv_sql$from_spec());
        Var var5 = const__15;
        var5.setMeta((IPersistentMap)const__17);
        Var var6 = var5;
        var5.bindRoot(const__18);
        Object object6 = ((IFn)const__19.getRawRoot()).invoke(const__20, const__21.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__22, new kv_sql$fn__11587()}));
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.kv-sql");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.require", (String)"maybe-require");
        const__5 = (AFn)Symbol.intern(null, (String)"datomic.kv-sql-ext");
        const__6 = RT.var((String)"datomic.kv-sql", (String)"constraint-violation?");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"e")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SQLException")}))))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.kv-sql", (String)"from-spec");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.kv-sql", (String)"LOGIN_FAILED");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__18 = 28000L;
        const__19 = RT.var((String)"clojure.core", (String)"extend");
        const__20 = RT.classForName((String)"java.sql.SQLException");
        const__21 = RT.var((String)"datomic.kv-store", (String)"Retryable");
        const__22 = RT.keyword(null, (String)"retryable?");
    }

    static {
        kv_sql__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.kv_sql__init").getClassLoader());
        try {
            kv_sql__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


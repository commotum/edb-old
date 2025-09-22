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
import datomic.db_io$db_resources;
import datomic.db_io$fn__17056;
import datomic.db_io$index_db;
import datomic.db_io$index_includes_some_log_tail_QMARK_;
import datomic.db_io$load_db_from_basis;
import datomic.db_io$loading__6434__auto____11067;
import datomic.db_io$log;
import datomic.db_io$most_current_db;
import datomic.db_io$storage_resources;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class db_io__init {
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
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__27;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new db_io$loading__6434__auto____11067()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new db_io$fn__17056())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)new db_io$storage_resources());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new db_io$index_db());
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot((Object)new db_io$log());
        Var var7 = const__15;
        var7.setMeta((IPersistentMap)const__17);
        Var var8 = var7;
        var7.bindRoot((Object)new db_io$db_resources());
        Var var9 = const__18;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot((Object)new db_io$index_includes_some_log_tail_QMARK_());
        Var var11 = const__21;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new db_io$most_current_db());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new db_io$load_db_from_basis());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.db-io");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.db-io", (String)"storage-resources");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.db-io", (String)"index-db");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"olookup"), (Object)Symbol.intern(null, (String)"resolved-conf"))})))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.db-io", (String)"log");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"olookup"))})))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.db-io", (String)"db-resources");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cr")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.db-io", (String)"index-includes-some-log-tail?");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"log"))})))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.db-io", (String)"most-current-db");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"dbs")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.db-io", (String)"load-db-from-basis");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-db-conn"), (Object)Symbol.intern(null, (String)"olookup"), (Object)Symbol.intern(null, (String)"db-id"), (Object)Symbol.intern(null, (String)"&"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"basis-db"))))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        db_io__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.db_io__init").getClassLoader());
        try {
            db_io__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


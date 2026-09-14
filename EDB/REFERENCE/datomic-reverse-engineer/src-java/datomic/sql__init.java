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
package datomic;

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
import datomic.sql$connect;
import datomic.sql$delete;
import datomic.sql$execute_commands;
import datomic.sql$fn__11479;
import datomic.sql$insert;
import datomic.sql$insert_with_nulls;
import datomic.sql$loading__6434__auto____11477;
import datomic.sql$select;
import datomic.sql$update;
import datomic.sql$update_with_nulls;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class sql__init {
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
    public static final Var const__19;
    public static final AFn const__21;
    public static final Var const__22;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__27;
    public static final Var const__28;
    public static final AFn const__30;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new sql$loading__6434__auto____11477()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new sql$fn__11479())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new sql$connect());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new sql$update_with_nulls());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new sql$insert_with_nulls());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new sql$update());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new sql$insert());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new sql$select());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new sql$delete());
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot((Object)new sql$execute_commands());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.sql");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.sql", (String)"connect");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"datasource")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"DataSource")})), (Object)Symbol.intern(null, (String)"factory")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"args")})))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.sql", (String)"update-with-nulls");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"spec"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"ensure-rev"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"rev"), (Object)Symbol.intern(null, (String)"map"), (Object)Symbol.intern(null, (String)"val"))})))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.sql", (String)"insert-with-nulls");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"spec"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"rev"), (Object)Symbol.intern(null, (String)"map"), (Object)Symbol.intern(null, (String)"val"))})))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.sql", (String)"update");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"spec"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"ensure-rev"), (Object)Symbol.intern(null, (String)"v-map")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.sql", (String)"insert");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"spec"), (Object)Symbol.intern(null, (String)"v-map")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.sql", (String)"select");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"spec"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.sql", (String)"delete");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"spec"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.sql", (String)"execute-commands");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"conn")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Connection")})), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"cmds")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        sql__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.sql__init").getClassLoader());
        try {
            sql__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


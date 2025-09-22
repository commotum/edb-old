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
import datomic.ddb_values$chunk;
import datomic.ddb_values$chunk_key;
import datomic.ddb_values$delete_value;
import datomic.ddb_values$fn__20367;
import datomic.ddb_values$fn__20383;
import datomic.ddb_values$get_deitem;
import datomic.ddb_values$get_value;
import datomic.ddb_values$loading__6434__auto____20365;
import datomic.ddb_values$put_value;
import datomic.ddb_values$split_map_keys_with;
import datomic.ddb_values$unchunk;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ddb_values__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__8;
    public static final Var const__9;
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
    public static final Var const__31;
    public static final AFn const__33;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new ddb_values$loading__6434__auto____20365()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new ddb_values$fn__20367())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4.setDynamic(true);
        Var var2 = var;
        var.setMeta((IPersistentMap)const__8);
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new ddb_values$chunk());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new ddb_values$unchunk());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new ddb_values$split_map_keys_with());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new ddb_values$chunk_key());
        Object object4 = ((IFn)new ddb_values$fn__20383()).invoke();
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new ddb_values$put_value());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new ddb_values$get_deitem());
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot((Object)new ddb_values$get_value());
        Var var17 = const__31;
        var17.setMeta((IPersistentMap)const__33);
        Var var18 = var17;
        var17.bindRoot((Object)new ddb_values$delete_value());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.ddb-values");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.ddb-values", (String)"*retry*");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"dynamic"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.ddb-values", (String)"chunk");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s"), (Object)Symbol.intern(null, (String)"chunk-size")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.ddb-values", (String)"unchunk");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"chunks")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.ddb-values", (String)"split-map-keys-with");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"pred")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.ddb-values", (String)"chunk-key");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"n")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.ddb-values", (String)"put-value");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ddb-client"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"value")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.ddb-values", (String)"get-deitem");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ddb-client"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.ddb-values", (String)"get-value");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ddb-client"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.ddb-values", (String)"delete-value");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ddb-client"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        ddb_values__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.ddb_values__init").getClassLoader());
        try {
            ddb_values__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


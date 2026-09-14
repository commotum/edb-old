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
import datomic.cassandra_v4$cql_delete;
import datomic.cassandra_v4$cql_insert;
import datomic.cassandra_v4$cql_select;
import datomic.cassandra_v4$cql_update;
import datomic.cassandra_v4$delete_stmt_STAR_;
import datomic.cassandra_v4$fn__10102;
import datomic.cassandra_v4$insert_stmt_STAR_;
import datomic.cassandra_v4$loading__6434__auto____10080;
import datomic.cassandra_v4$row__GT_map;
import datomic.cassandra_v4$select_stmt_STAR_;
import datomic.cassandra_v4$select_string;
import datomic.cassandra_v4$select_with_consistency;
import datomic.cassandra_v4$session_from_callback;
import datomic.cassandra_v4$update_stmt_STAR_;
import datomic.cassandra_v4$updated_QMARK_;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class cassandra_v4__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final AFn const__15;
    public static final Var const__16;
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__21;
    public static final Var const__22;
    public static final AFn const__23;
    public static final Var const__24;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;
    public static final Var const__30;
    public static final AFn const__32;
    public static final Var const__33;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__37;
    public static final Var const__38;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__48;
    public static final Var const__49;
    public static final AFn const__51;
    public static final Var const__52;
    public static final AFn const__54;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new cassandra_v4$loading__6434__auto____10080()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new cassandra_v4$fn__10102())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new cassandra_v4$update_stmt_STAR_());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot(((IFn)const__12.getRawRoot()).invoke(const__4.getRawRoot()));
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new cassandra_v4$updated_QMARK_());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new cassandra_v4$cql_update());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new cassandra_v4$insert_stmt_STAR_());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__23);
        Var var12 = var11;
        var11.bindRoot(((IFn)const__12.getRawRoot()).invoke(const__19.getRawRoot()));
        Var var13 = const__24;
        var13.setMeta((IPersistentMap)const__26);
        Var var14 = var13;
        var13.bindRoot((Object)new cassandra_v4$cql_insert());
        Var var15 = const__27;
        var15.setMeta((IPersistentMap)const__29);
        Var var16 = var15;
        var15.bindRoot((Object)new cassandra_v4$select_string());
        Var var17 = const__30;
        var17.setMeta((IPersistentMap)const__32);
        Var var18 = var17;
        var17.bindRoot((Object)new cassandra_v4$select_stmt_STAR_());
        Var var19 = const__33;
        var19.setMeta((IPersistentMap)const__34);
        Var var20 = var19;
        var19.bindRoot(((IFn)const__12.getRawRoot()).invoke(const__30.getRawRoot()));
        Var var21 = const__35;
        var21.setMeta((IPersistentMap)const__37);
        Var var22 = var21;
        var21.bindRoot((Object)new cassandra_v4$select_with_consistency());
        Var var23 = const__38;
        var23.setMeta((IPersistentMap)const__40);
        Var var24 = var23;
        var23.bindRoot((Object)new cassandra_v4$row__GT_map());
        Var var25 = const__41;
        var25.setMeta((IPersistentMap)const__43);
        Var var26 = var25;
        var25.bindRoot((Object)new cassandra_v4$cql_select());
        Var var27 = const__44;
        var27.setMeta((IPersistentMap)const__46);
        Var var28 = var27;
        var27.bindRoot((Object)new cassandra_v4$delete_stmt_STAR_());
        Var var29 = const__47;
        var29.setMeta((IPersistentMap)const__48);
        Var var30 = var29;
        var29.bindRoot(((IFn)const__12.getRawRoot()).invoke(const__44.getRawRoot()));
        Var var31 = const__49;
        var31.setMeta((IPersistentMap)const__51);
        Var var32 = var31;
        var31.bindRoot((Object)new cassandra_v4$cql_delete());
        Var var33 = const__52;
        var33.setMeta((IPersistentMap)const__54);
        Var var34 = var33;
        var33.bindRoot((Object)new cassandra_v4$session_from_callback());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cassandra-v4");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.cassandra-v4", (String)"update-stmt*");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id-key"), (Object)Symbol.intern(null, (String)"col-names")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.cassandra-v4", (String)"update-stmt");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"clojure.core", (String)"memoize");
        const__13 = RT.var((String)"datomic.cassandra-v4", (String)"updated?");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"res")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ResultSet")}))))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.cassandra-v4", (String)"cql-update");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"ensure-rev"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"id-key"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"ks")), (Object)Symbol.intern(null, (String)"v-map")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.cassandra-v4", (String)"insert-stmt*");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"col-names"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.cassandra-v4", (String)"insert-stmt");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__24 = RT.var((String)"datomic.cassandra-v4", (String)"cql-insert");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"ks"), (Object)Symbol.intern(null, (String)"v-map"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.cassandra-v4", (String)"select-string");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"table"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"id-key"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"ks")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.String")})))), RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"datomic.cassandra-v4", (String)"select-stmt*");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"ks")))), RT.keyword(null, (String)"column"), 1});
        const__33 = RT.var((String)"datomic.cassandra-v4", (String)"select-stmt");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.cassandra-v4", (String)"select-with-consistency");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)((IObj)Symbol.intern(null, (String)"stmt")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"PreparedStatement")})), (Object)Symbol.intern(null, (String)"consistency"), (Object)Symbol.intern(null, (String)"serial"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__38 = RT.var((String)"datomic.cassandra-v4", (String)"row->map");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"row")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Row")})), (Object)Symbol.intern(null, (String)"ks")))), RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.cassandra-v4", (String)"cql-select");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"ks"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.cassandra-v4", (String)"delete-stmt*");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id-key")))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.cassandra-v4", (String)"delete-stmt");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"datomic.cassandra-v4", (String)"cql-delete");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"SyncCqlSession")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"id-key"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"_"))))), RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.cassandra-v4", (String)"session-from-callback");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"session-callback")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"endpoint")})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        cassandra_v4__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cassandra_v4__init").getClassLoader());
        try {
            cassandra_v4__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


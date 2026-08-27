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
import datomic.cassandra$cluster_from_callback;
import datomic.cassandra$cql_delete;
import datomic.cassandra$cql_insert;
import datomic.cassandra$cql_select;
import datomic.cassandra$cql_update;
import datomic.cassandra$delete_stmt_STAR_;
import datomic.cassandra$fn__17725;
import datomic.cassandra$insert_stmt_STAR_;
import datomic.cassandra$loading__6434__auto____17723;
import datomic.cassandra$retry_policy;
import datomic.cassandra$row__GT_map;
import datomic.cassandra$select_stmt_STAR_;
import datomic.cassandra$select_string;
import datomic.cassandra$select_with_consistency;
import datomic.cassandra$update_stmt_STAR_;
import datomic.cassandra$updated_QMARK_;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class cassandra__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__21;
    public static final Var const__22;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;
    public static final Var const__30;
    public static final AFn const__32;
    public static final Var const__33;
    public static final AFn const__35;
    public static final Var const__36;
    public static final AFn const__37;
    public static final Var const__38;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__51;
    public static final Var const__52;
    public static final AFn const__54;
    public static final Var const__55;
    public static final AFn const__57;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new cassandra$loading__6434__auto____17723()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new cassandra$fn__17725())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new cassandra$retry_policy());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new cassandra$update_stmt_STAR_());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot(((IFn)const__15.getRawRoot()).invoke(const__10.getRawRoot()));
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new cassandra$updated_QMARK_());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new cassandra$cql_update());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new cassandra$insert_stmt_STAR_());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__26);
        Var var14 = var13;
        var13.bindRoot(((IFn)const__15.getRawRoot()).invoke(const__22.getRawRoot()));
        Var var15 = const__27;
        var15.setMeta((IPersistentMap)const__29);
        Var var16 = var15;
        var15.bindRoot((Object)new cassandra$cql_insert());
        Var var17 = const__30;
        var17.setMeta((IPersistentMap)const__32);
        Var var18 = var17;
        var17.bindRoot((Object)new cassandra$select_string());
        Var var19 = const__33;
        var19.setMeta((IPersistentMap)const__35);
        Var var20 = var19;
        var19.bindRoot((Object)new cassandra$select_stmt_STAR_());
        Var var21 = const__36;
        var21.setMeta((IPersistentMap)const__37);
        Var var22 = var21;
        var21.bindRoot(((IFn)const__15.getRawRoot()).invoke(const__33.getRawRoot()));
        Var var23 = const__38;
        var23.setMeta((IPersistentMap)const__40);
        Var var24 = var23;
        var23.bindRoot((Object)new cassandra$select_with_consistency());
        Var var25 = const__41;
        var25.setMeta((IPersistentMap)const__43);
        Var var26 = var25;
        var25.bindRoot((Object)new cassandra$row__GT_map());
        Var var27 = const__44;
        var27.setMeta((IPersistentMap)const__46);
        Var var28 = var27;
        var27.bindRoot((Object)new cassandra$cql_select());
        Var var29 = const__47;
        var29.setMeta((IPersistentMap)const__49);
        Var var30 = var29;
        var29.bindRoot((Object)new cassandra$delete_stmt_STAR_());
        Var var31 = const__50;
        var31.setMeta((IPersistentMap)const__51);
        Var var32 = var31;
        var31.bindRoot(((IFn)const__15.getRawRoot()).invoke(const__47.getRawRoot()));
        Var var33 = const__52;
        var33.setMeta((IPersistentMap)const__54);
        Var var34 = var33;
        var33.bindRoot((Object)new cassandra$cql_delete());
        Var var35 = const__55;
        var35.setMeta((IPersistentMap)const__57);
        Var var36 = var35;
        var35.bindRoot((Object)new cassandra$cluster_from_callback());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cassandra");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.cassandra", (String)"retry-policy");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.cassandra", (String)"update-stmt*");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Session")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id-key"), (Object)Symbol.intern(null, (String)"col-names")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.cassandra", (String)"update-stmt");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"clojure.core", (String)"memoize");
        const__16 = RT.var((String)"datomic.cassandra", (String)"updated?");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"res")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ResultSet")}))))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.cassandra", (String)"cql-update");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Session")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"ensure-rev"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"id-key"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"ks")), (Object)Symbol.intern(null, (String)"v-map")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.cassandra", (String)"insert-stmt*");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Session")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"col-names"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.cassandra", (String)"insert-stmt");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.cassandra", (String)"cql-insert");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Session")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"ks"), (Object)Symbol.intern(null, (String)"v-map"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"datomic.cassandra", (String)"select-string");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"table"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"id-key"), (Object)RT.keyword(null, (String)"as"), (Object)Symbol.intern(null, (String)"ks")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.String")})))), RT.keyword(null, (String)"column"), 1});
        const__33 = RT.var((String)"datomic.cassandra", (String)"select-stmt*");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Session")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"ks")))), RT.keyword(null, (String)"column"), 1});
        const__36 = RT.var((String)"datomic.cassandra", (String)"select-stmt");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__38 = RT.var((String)"datomic.cassandra", (String)"select-with-consistency");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Session")})), (Object)((IObj)Symbol.intern(null, (String)"stmt")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"PreparedStatement")})), (Object)Symbol.intern(null, (String)"consistency"), (Object)Symbol.intern(null, (String)"serial"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.cassandra", (String)"row->map");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"row")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Row")})), (Object)Symbol.intern(null, (String)"ks")))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.cassandra", (String)"cql-select");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Session")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"ks"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.cassandra", (String)"delete-stmt*");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Session")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id-key")))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.cassandra", (String)"delete-stmt");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.cassandra", (String)"cql-delete");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Session")})), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id"), (Object)Tuple.create((Object)Symbol.intern(null, (String)"id-key"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"ks"))))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.cassandra", (String)"cluster-from-callback");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"cluster-callback")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"endpoint")})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        cassandra__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cassandra__init").getClassLoader());
        try {
            cassandra__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


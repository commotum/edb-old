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
import datomic.catalog$add_database;
import datomic.catalog$conflict_check_fn;
import datomic.catalog$create_database;
import datomic.catalog$create_database_STAR_;
import datomic.catalog$db_id__GT_db_name;
import datomic.catalog$db_ids;
import datomic.catalog$db_name__GT_db_id;
import datomic.catalog$db_names;
import datomic.catalog$delete;
import datomic.catalog$delete_database;
import datomic.catalog$deleted_QMARK_;
import datomic.catalog$deleted_database_id_QMARK_;
import datomic.catalog$fn__11073;
import datomic.catalog$get_catalog;
import datomic.catalog$get_database_names;
import datomic.catalog$loading__6434__auto____11071;
import datomic.catalog$parse_db_conf;
import datomic.catalog$pod__GT_catalog;
import datomic.catalog$put_catalog;
import datomic.catalog$remove_deleted;
import datomic.catalog$remove_deleted_database;
import datomic.catalog$rename;
import datomic.catalog$rename_database;
import datomic.catalog$undelete_database;
import datomic.catalog$update_catalog;
import datomic.catalog$update_succeeded_QMARK_;
import datomic.catalog$valid_db_name_QMARK_;
import datomic.catalog$with_retry;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class catalog__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__7;
    public static final Var const__8;
    public static final AFn const__11;
    public static final Var const__12;
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
    public static final Var const__34;
    public static final AFn const__36;
    public static final Var const__37;
    public static final AFn const__39;
    public static final Var const__40;
    public static final AFn const__42;
    public static final Var const__43;
    public static final AFn const__45;
    public static final Var const__46;
    public static final AFn const__48;
    public static final Var const__49;
    public static final AFn const__51;
    public static final Var const__52;
    public static final AFn const__54;
    public static final Var const__55;
    public static final AFn const__57;
    public static final Var const__58;
    public static final AFn const__60;
    public static final Var const__61;
    public static final AFn const__63;
    public static final Var const__64;
    public static final AFn const__66;
    public static final Var const__67;
    public static final AFn const__69;
    public static final Var const__70;
    public static final AFn const__72;
    public static final Var const__73;
    public static final AFn const__75;
    public static final Var const__76;
    public static final AFn const__78;
    public static final Var const__79;
    public static final AFn const__81;
    public static final Var const__82;
    public static final AFn const__84;
    public static final Var const__85;
    public static final AFn const__87;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new catalog$loading__6434__auto____11071()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new catalog$fn__11073())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot((Object)"pod-catalog");
        Var var3 = const__8;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new catalog$pod__GT_catalog());
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new catalog$valid_db_name_QMARK_());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new catalog$get_catalog());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new catalog$put_catalog());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new catalog$db_names());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new catalog$get_database_names());
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot((Object)new catalog$db_ids());
        Var var17 = const__31;
        var17.setMeta((IPersistentMap)const__33);
        Var var18 = var17;
        var17.bindRoot((Object)new catalog$db_id__GT_db_name());
        Var var19 = const__34;
        var19.setMeta((IPersistentMap)const__36);
        Var var20 = var19;
        var19.bindRoot((Object)new catalog$db_name__GT_db_id());
        Var var21 = const__37;
        var21.setMeta((IPersistentMap)const__39);
        Var var22 = var21;
        var21.bindRoot((Object)new catalog$with_retry());
        const__37.setMacro();
        Object v25 = null;
        Var var23 = const__37;
        Var var24 = const__40;
        var24.setMeta((IPersistentMap)const__42);
        Var var25 = var24;
        var24.bindRoot((Object)new catalog$update_catalog());
        Var var26 = const__43;
        var26.setMeta((IPersistentMap)const__45);
        Var var27 = var26;
        var26.bindRoot((Object)new catalog$update_succeeded_QMARK_());
        Var var28 = const__46;
        var28.setMeta((IPersistentMap)const__48);
        Var var29 = var28;
        var28.bindRoot((Object)new catalog$conflict_check_fn());
        Var var30 = const__49;
        var30.setMeta((IPersistentMap)const__51);
        Var var31 = var30;
        var30.bindRoot((Object)new catalog$add_database());
        Var var32 = const__52;
        var32.setMeta((IPersistentMap)const__54);
        Var var33 = var32;
        var32.bindRoot((Object)new catalog$create_database_STAR_());
        Var var34 = const__55;
        var34.setMeta((IPersistentMap)const__57);
        Var var35 = var34;
        var34.bindRoot((Object)new catalog$create_database());
        Var var36 = const__58;
        var36.setMeta((IPersistentMap)const__60);
        Var var37 = var36;
        var36.bindRoot((Object)new catalog$rename());
        Var var38 = const__61;
        var38.setMeta((IPersistentMap)const__63);
        Var var39 = var38;
        var38.bindRoot((Object)new catalog$rename_database());
        Var var40 = const__64;
        var40.setMeta((IPersistentMap)const__66);
        Var var41 = var40;
        var40.bindRoot((Object)new catalog$delete());
        Var var42 = const__67;
        var42.setMeta((IPersistentMap)const__69);
        Var var43 = var42;
        var42.bindRoot((Object)new catalog$delete_database());
        Var var44 = const__70;
        var44.setMeta((IPersistentMap)const__72);
        Var var45 = var44;
        var44.bindRoot((Object)new catalog$undelete_database());
        Var var46 = const__73;
        var46.setMeta((IPersistentMap)const__75);
        Var var47 = var46;
        var46.bindRoot((Object)new catalog$remove_deleted());
        Var var48 = const__76;
        var48.setMeta((IPersistentMap)const__78);
        Var var49 = var48;
        var48.bindRoot((Object)new catalog$remove_deleted_database());
        Var var50 = const__79;
        var50.setMeta((IPersistentMap)const__81);
        Var var51 = var50;
        var50.bindRoot((Object)new catalog$deleted_QMARK_());
        Var var52 = const__82;
        var52.setMeta((IPersistentMap)const__84);
        Var var53 = var52;
        var52.bindRoot((Object)new catalog$deleted_database_id_QMARK_());
        Var var54 = const__85;
        var54.setMeta((IPersistentMap)const__87);
        Var var55 = var54;
        var54.bindRoot((Object)new catalog$parse_db_conf());
        Object v59 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.catalog");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.catalog", (String)"catalog-key");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__8 = RT.var((String)"datomic.catalog", (String)"pod->catalog");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"pod")))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.catalog", (String)"valid-db-name?");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db-name")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.catalog", (String)"get-catalog");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.catalog", (String)"put-catalog");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"catalog-map")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.catalog", (String)"db-names");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"catalog")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.catalog", (String)"get-database-names");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.catalog", (String)"db-ids");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"catalog")))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.catalog", (String)"db-id->db-name");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"catalog"), (Object)Symbol.intern(null, (String)"db-id")))), RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"datomic.catalog", (String)"db-name->db-id");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"catalog"), (Object)Symbol.intern(null, (String)"db-name")))), RT.keyword(null, (String)"column"), 1});
        const__37 = RT.var((String)"datomic.catalog", (String)"with-retry");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__40 = RT.var((String)"datomic.catalog", (String)"update-catalog");
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"condition"), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__43 = RT.var((String)"datomic.catalog", (String)"update-succeeded?");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__46 = RT.var((String)"datomic.catalog", (String)"conflict-check-fn");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db-name"), (Object)Symbol.intern(null, (String)"db-id")))), RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"datomic.catalog", (String)"add-database");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"catalog"), (Object)Symbol.intern(null, (String)"db-name"), (Object)Symbol.intern(null, (String)"db-id")))), RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.catalog", (String)"create-database*");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"db-name"), (Object)Symbol.intern(null, (String)"db-id"))})))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.catalog", (String)"create-database");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"system-cluster"), (Object)Symbol.intern(null, (String)"desc")))), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.catalog", (String)"rename");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"catalog"), (Object)Symbol.intern(null, (String)"db-name"), (Object)Symbol.intern(null, (String)"new-name")))), RT.keyword(null, (String)"column"), 1});
        const__61 = RT.var((String)"datomic.catalog", (String)"rename-database");
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"db-name"), (Object)Symbol.intern(null, (String)"new-name")))), RT.keyword(null, (String)"column"), 1});
        const__64 = RT.var((String)"datomic.catalog", (String)"delete");
        const__66 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"catalog"), (Object)Symbol.intern(null, (String)"db-name")))), RT.keyword(null, (String)"column"), 1});
        const__67 = RT.var((String)"datomic.catalog", (String)"delete-database");
        const__69 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"db-name")))), RT.keyword(null, (String)"column"), 1});
        const__70 = RT.var((String)"datomic.catalog", (String)"undelete-database");
        const__72 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"db-id"), (Object)Symbol.intern(null, (String)"db-name")))), RT.keyword(null, (String)"column"), 1});
        const__73 = RT.var((String)"datomic.catalog", (String)"remove-deleted");
        const__75 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"catalog"), (Object)Symbol.intern(null, (String)"db-id")))), RT.keyword(null, (String)"column"), 1});
        const__76 = RT.var((String)"datomic.catalog", (String)"remove-deleted-database");
        const__78 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"db-id")))), RT.keyword(null, (String)"column"), 1});
        const__79 = RT.var((String)"datomic.catalog", (String)"deleted?");
        const__81 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"catalog"), (Object)Symbol.intern(null, (String)"db-id")))), RT.keyword(null, (String)"column"), 1});
        const__82 = RT.var((String)"datomic.catalog", (String)"deleted-database-id?");
        const__84 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"db-id")))), RT.keyword(null, (String)"column"), 1});
        const__85 = RT.var((String)"datomic.catalog", (String)"parse-db-conf");
        const__87 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db-conf")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        catalog__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.catalog__init").getClassLoader());
        try {
            catalog__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


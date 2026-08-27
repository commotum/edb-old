/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AReference
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AReference;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.ddb_s3_cluster$create_connection;
import datomic.ddb_s3_cluster$create_connection_with_efs;
import datomic.ddb_s3_cluster$create_connection_without_efs;
import datomic.ddb_s3_cluster$create_s3_store;
import datomic.ddb_s3_cluster$ensure_config;
import datomic.ddb_s3_cluster$ensure_system;
import datomic.ddb_s3_cluster$fn__22742;
import datomic.ddb_s3_cluster$fn__22744;
import datomic.ddb_s3_cluster$fn__22749;
import datomic.ddb_s3_cluster$fn__22754;
import datomic.ddb_s3_cluster$fn__22759;
import datomic.ddb_s3_cluster$fn__22764;
import datomic.ddb_s3_cluster$get_config_refval;
import datomic.ddb_s3_cluster$get_valid_config_BANG_;
import datomic.ddb_s3_cluster$loading__6434__auto____22590;
import datomic.ddb_s3_cluster$storage_path;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ddb_s3_cluster__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Var const__5;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__23;
    public static final Var const__24;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;
    public static final Var const__30;
    public static final AFn const__32;
    public static final Var const__33;
    public static final AFn const__35;
    public static final Var const__36;
    public static final AFn const__38;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__3);
        Object object2 = ((IFn)new ddb_s3_cluster$loading__6434__auto____22590()).invoke();
        if (((Symbol)const__1).equals((Object)const__4)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new ddb_s3_cluster$fn__22742())));
            v3 = null;
        }
        Var var = const__5;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)"ref-storage-config");
        Object object3 = ((IFn)new ddb_s3_cluster$fn__22744()).invoke();
        Object object4 = ((IFn)new ddb_s3_cluster$fn__22749()).invoke();
        Object object5 = ((IFn)new ddb_s3_cluster$fn__22754()).invoke();
        Object object6 = ((IFn)new ddb_s3_cluster$fn__22759()).invoke();
        Object object7 = ((IFn)new ddb_s3_cluster$fn__22764()).invoke();
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__14);
        Var var4 = var3;
        var3.bindRoot((Object)new ddb_s3_cluster$storage_path());
        Var var5 = const__15;
        var5.setMeta((IPersistentMap)const__17);
        Var var6 = var5;
        var5.bindRoot((Object)new ddb_s3_cluster$get_config_refval());
        Var var7 = const__18;
        var7.setMeta((IPersistentMap)const__20);
        Var var8 = var7;
        var7.bindRoot((Object)new ddb_s3_cluster$get_valid_config_BANG_());
        Var var9 = const__21;
        var9.setMeta((IPersistentMap)const__23);
        Var var10 = var9;
        var9.bindRoot((Object)new ddb_s3_cluster$ensure_config());
        Var var11 = const__24;
        var11.setMeta((IPersistentMap)const__26);
        Var var12 = var11;
        var11.bindRoot((Object)new ddb_s3_cluster$create_s3_store());
        Var var13 = const__27;
        var13.setMeta((IPersistentMap)const__29);
        Var var14 = var13;
        var13.bindRoot((Object)new ddb_s3_cluster$create_connection_with_efs());
        Var var15 = const__30;
        var15.setMeta((IPersistentMap)const__32);
        Var var16 = var15;
        var15.bindRoot((Object)new ddb_s3_cluster$create_connection_without_efs());
        Var var17 = const__33;
        var17.setMeta((IPersistentMap)const__35);
        Var var18 = var17;
        var17.bindRoot((Object)new ddb_s3_cluster$create_connection());
        Var var19 = const__36;
        var19.setMeta((IPersistentMap)const__38);
        Var var20 = var19;
        var19.bindRoot((Object)new ddb_s3_cluster$ensure_system());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.ddb-s3-cluster")).withMeta(RT.map((Object[])new Object[0]));
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "A ClusteredStore that writes refs to Dynamo\nand values to EFS+S3."});
        const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__5 = RT.var((String)"datomic.ddb-s3-cluster", (String)"storage-config-key");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.ddb-s3-cluster", (String)"storage-path");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"system"), (Object)Symbol.intern(null, (String)"db-id"))})))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.ddb-s3-cluster", (String)"get-config-refval");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"kv-cluster")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.ddb-s3-cluster", (String)"get-valid-config!");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"kv-store"), (Object)Symbol.intern(null, (String)"cluster-conf")))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.ddb-s3-cluster", (String)"ensure-config");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"bucket"), Symbol.intern(null, (String)"bucket-prefix"), Symbol.intern(null, (String)"force"), Symbol.intern(null, (String)"efs-path"), Symbol.intern(null, (String)"region"), Symbol.intern(null, (String)"system"), Symbol.intern(null, (String)"table-name")}), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"storage-config"), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"system"), Symbol.intern((String)"common", (String)"DEFAULT_SYSTEM_NAME"), Symbol.intern(null, (String)"force"), Boolean.FALSE})})))), RT.keyword(null, (String)"column"), 1});
        const__24 = RT.var((String)"datomic.ddb-s3-cluster", (String)"create-s3-store");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"aws-region"), (Object)Symbol.intern(null, (String)"s3-vals-bucket"), (Object)Symbol.intern(null, (String)"s3-vals-prefix")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cluster-conf")})))), RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.ddb-s3-cluster", (String)"create-connection-with-efs");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"near-store-get-timeout-msec"), (Object)Symbol.intern(null, (String)"aws-region"), (Object)Symbol.intern(null, (String)"aws-dynamodb-table"), (Object)Symbol.intern(null, (String)"system")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cluster-conf"), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"near-store-get-timeout-msec"), 20L})})))), RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"datomic.ddb-s3-cluster", (String)"create-connection-without-efs");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"aws-region"), (Object)Symbol.intern(null, (String)"aws-dynamodb-table"), (Object)Symbol.intern(null, (String)"system")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cluster-conf")})))), RT.keyword(null, (String)"column"), 1});
        const__33 = RT.var((String)"datomic.ddb-s3-cluster", (String)"create-connection");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"skip-efs")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cluster-conf")})))), RT.keyword(null, (String)"column"), 1});
        const__36 = RT.var((String)"datomic.ddb-s3-cluster", (String)"ensure-system");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"table-name")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"storage-config")})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        ddb_s3_cluster__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.ddb_s3_cluster__init").getClassLoader());
        try {
            ddb_s3_cluster__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


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
import datomic.h2$can_remote_QMARK_;
import datomic.h2$create_sql_spec;
import datomic.h2$ensure_admin_conn;
import datomic.h2$ensure_datomic_password;
import datomic.h2$fn__11589;
import datomic.h2$fn__11592;
import datomic.h2$init_embedded;
import datomic.h2$init_tcp;
import datomic.h2$loading__6434__auto____11475;
import datomic.h2$local_jdbc_spec;
import datomic.h2$remote_jdbc_spec;
import datomic.h2$rename_user_cmd;
import datomic.h2$set_password_cmd;
import datomic.h2$shutdown;
import datomic.h2$sql_url;
import datomic.h2$try_connect;
import datomic.h2$updating_connect;
import datomic.h2$uq;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class h2__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__8;
    public static final Var const__9;
    public static final AFn const__10;
    public static final Var const__11;
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

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new h2$loading__6434__auto____11475()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new h2$fn__11589())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot(new Object());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__10);
        Var var4 = var3;
        var3.bindRoot(((IFn)const__11.getRawRoot()).invoke((Object)new h2$fn__11592()));
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new h2$create_sql_spec());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new h2$sql_url());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new h2$try_connect());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new h2$uq());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new h2$rename_user_cmd());
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot((Object)new h2$set_password_cmd());
        Var var17 = const__31;
        var17.setMeta((IPersistentMap)const__33);
        Var var18 = var17;
        var17.bindRoot((Object)new h2$updating_connect());
        Var var19 = const__34;
        var19.setMeta((IPersistentMap)const__36);
        Var var20 = var19;
        var19.bindRoot((Object)new h2$ensure_admin_conn());
        Var var21 = const__37;
        var21.setMeta((IPersistentMap)const__39);
        Var var22 = var21;
        var21.bindRoot((Object)new h2$ensure_datomic_password());
        Var var23 = const__40;
        var23.setMeta((IPersistentMap)const__42);
        Var var24 = var23;
        var23.bindRoot((Object)new h2$can_remote_QMARK_());
        Var var25 = const__43;
        var25.setMeta((IPersistentMap)const__45);
        Var var26 = var25;
        var25.bindRoot((Object)new h2$init_embedded());
        Var var27 = const__46;
        var27.setMeta((IPersistentMap)const__48);
        Var var28 = var27;
        var27.bindRoot((Object)new h2$init_tcp());
        Var var29 = const__49;
        var29.setMeta((IPersistentMap)const__51);
        Var var30 = var29;
        var29.bindRoot((Object)new h2$shutdown());
        Var var31 = const__52;
        var31.setMeta((IPersistentMap)const__54);
        Var var32 = var31;
        var31.bindRoot((Object)new h2$local_jdbc_spec());
        Var var33 = const__55;
        var33.setMeta((IPersistentMap)const__57);
        Var var34 = var33;
        var33.bindRoot((Object)new h2$remote_jdbc_spec());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.h2");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.h2", (String)"driver-manager-lock");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.h2", (String)"create-sql-spec*");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"clojure.core", (String)"memoize");
        const__12 = RT.var((String)"datomic.h2", (String)"create-sql-spec");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-map")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.h2", (String)"sql-url");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"data-dir")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.h2", (String)"try-connect");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"data-dir"), (Object)Symbol.intern(null, (String)"username"), (Object)Symbol.intern(null, (String)"password"))})))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.h2", (String)"uq");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"username")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.h2", (String)"rename-user-cmd");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"old-user"), (Object)Symbol.intern(null, (String)"user"))})))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.h2", (String)"set-password-cmd");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"user"), (Object)Symbol.intern(null, (String)"password"))})))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.h2", (String)"updating-connect");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"data-dir"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"user"), (Object)Symbol.intern(null, (String)"password"), (Object)Symbol.intern(null, (String)"old-user"), (Object)Symbol.intern(null, (String)"old-password")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"args")})))), RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"datomic.h2", (String)"ensure-admin-conn");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"data-dir"), (Object)Symbol.intern(null, (String)"storage-admin-password"), (Object)Symbol.intern(null, (String)"old-storage-admin-password")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cluster-map")})))), RT.keyword(null, (String)"column"), 1});
        const__37 = RT.var((String)"datomic.h2", (String)"ensure-datomic-password");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"data-dir"), (Object)Symbol.intern(null, (String)"storage-datomic-password"), (Object)Symbol.intern(null, (String)"old-storage-datomic-password")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cluster-map")})))), RT.keyword(null, (String)"column"), 1});
        const__40 = RT.var((String)"datomic.h2", (String)"can-remote?");
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"storage-access"), (Object)Symbol.intern(null, (String)"storage-datomic-password"), (Object)Symbol.intern(null, (String)"storage-admin-password")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"h2-init-spec")})))), RT.keyword(null, (String)"column"), 1});
        const__43 = RT.var((String)"datomic.h2", (String)"init-embedded");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__46 = RT.var((String)"datomic.h2", (String)"init-tcp");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"host"), (Object)Symbol.intern(null, (String)"h2-port"), (Object)Symbol.intern(null, (String)"data-dir")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"spec")})))), RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"datomic.h2", (String)"shutdown");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"server")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Server")})))})))), RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.h2", (String)"local-jdbc-spec");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"data-dir"), (Object)Symbol.intern(null, (String)"storage-datomic-password")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cluster-map")})))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.h2", (String)"remote-jdbc-spec");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"host"), (Object)Symbol.intern(null, (String)"h2-port"), (Object)Symbol.intern(null, (String)"password"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        h2__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.h2__init").getClassLoader());
        try {
            h2__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


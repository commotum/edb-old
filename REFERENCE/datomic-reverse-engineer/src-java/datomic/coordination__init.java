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
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.coordination$allowed_valcache_client_QMARK_;
import datomic.coordination$check_peer_version;
import datomic.coordination$cluster_conf__GT_resolved_conf;
import datomic.coordination$create_db_cluster;
import datomic.coordination$create_dev_cluster;
import datomic.coordination$create_heartbeat;
import datomic.coordination$create_system_cluster;
import datomic.coordination$endpoint__GT_server_spec;
import datomic.coordination$fn__11656;
import datomic.coordination$fn__11658;
import datomic.coordination$fn__11665;
import datomic.coordination$fn__11667;
import datomic.coordination$fn__11671;
import datomic.coordination$fn__11675;
import datomic.coordination$fn__11677;
import datomic.coordination$heartbeat__GT_endpoint;
import datomic.coordination$init_dev;
import datomic.coordination$loading__6434__auto____11069;
import datomic.coordination$lookup_compatible_transactor_endpoint;
import datomic.coordination$lookup_endpoint;
import datomic.coordination$lookup_transactor_endpoint;
import datomic.coordination$resolve_db_name;
import datomic.coordination$vc_password;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class coordination__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__7;
    public static final Object const__8;
    public static final Var const__9;
    public static final AFn const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__15;
    public static final Var const__16;
    public static final Keyword const__17;
    public static final Keyword const__18;
    public static final Var const__19;
    public static final AFn const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final AFn const__25;
    public static final Var const__26;
    public static final AFn const__28;
    public static final Var const__29;
    public static final AFn const__30;
    public static final Var const__31;
    public static final AFn const__32;
    public static final Var const__33;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__36;
    public static final Keyword const__37;
    public static final Keyword const__38;
    public static final Var const__39;
    public static final AFn const__40;
    public static final Object const__41;
    public static final Var const__42;
    public static final AFn const__44;
    public static final Var const__45;
    public static final AFn const__47;
    public static final Var const__48;
    public static final AFn const__50;
    public static final Var const__51;
    public static final AFn const__53;
    public static final Var const__54;
    public static final AFn const__56;
    public static final Var const__57;
    public static final AFn const__59;
    public static final Var const__60;
    public static final AFn const__62;
    public static final Var const__63;
    public static final AFn const__65;
    public static final Var const__66;
    public static final AFn const__68;
    public static final Var const__69;
    public static final AFn const__71;
    public static final Var const__72;
    public static final AFn const__73;
    public static final Var const__74;
    public static final Var const__75;
    public static final Var const__76;
    public static final Object const__77;
    public static final Object const__78;
    public static final Var const__79;
    public static final AFn const__81;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new coordination$loading__6434__auto____11069()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new coordination$fn__11656())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot(const__8);
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__10);
        Var var4 = var3;
        var3.bindRoot(((IFn)const__11.getRawRoot()).invoke(null));
        Object object3 = ((IFn)new coordination$fn__11658()).invoke();
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new coordination$init_dev());
        MultiFn multiFn = ((MultiFn)const__16.getRawRoot()).addMethod((Object)const__17, (IFn)new coordination$fn__11665());
        MultiFn multiFn2 = ((MultiFn)const__16.getRawRoot()).addMethod((Object)const__18, (IFn)new coordination$fn__11667());
        Var var7 = const__19;
        var7.setMeta((IPersistentMap)const__21);
        Var var8 = var7;
        var7.bindRoot((Object)new coordination$create_dev_cluster());
        Object object4 = ((IFn)new coordination$fn__11671()).invoke();
        MultiFn multiFn3 = ((MultiFn)const__22.getRawRoot()).addMethod((Object)const__17, (IFn)new coordination$fn__11675());
        MultiFn multiFn4 = ((MultiFn)const__22.getRawRoot()).addMethod((Object)const__18, (IFn)new coordination$fn__11677());
        Var var9 = const__23;
        var9.setMeta((IPersistentMap)const__25);
        Var var10 = var9;
        var9.bindRoot((Object)new coordination$create_db_cluster());
        Var var11 = const__26;
        var11.setMeta((IPersistentMap)const__28);
        Var var12 = var11;
        var11.bindRoot((Object)new coordination$create_system_cluster());
        Var var13 = const__29;
        var13.setMeta((IPersistentMap)const__30);
        Var var14 = var13;
        var13.bindRoot((Object)"pod-coord");
        Var var15 = const__31;
        var15.setMeta((IPersistentMap)const__32);
        Var var16 = var15;
        var15.bindRoot((Object)"pod-standby");
        Var var17 = const__33;
        var17.setMeta((IPersistentMap)const__34);
        Var var18 = var17;
        var17.bindRoot((Object)RT.set((Object[])new Object[]{const__29.getRawRoot(), const__31.getRawRoot()}));
        Var var19 = const__35;
        var19.setMeta((IPersistentMap)const__36);
        Var var20 = var19;
        var19.bindRoot((Object)RT.mapUniqueKeys((Object[])new Object[]{const__37, const__29.getRawRoot(), const__38, const__31.getRawRoot()}));
        Var var21 = const__39;
        var21.setMeta((IPersistentMap)const__40);
        Var var22 = var21;
        var21.bindRoot(const__41);
        Var var23 = const__42;
        var23.setMeta((IPersistentMap)const__44);
        Var var24 = var23;
        var23.bindRoot((Object)new coordination$create_heartbeat());
        Var var25 = const__45;
        var25.setMeta((IPersistentMap)const__47);
        Var var26 = var25;
        var25.bindRoot((Object)new coordination$heartbeat__GT_endpoint());
        Var var27 = const__48;
        var27.setMeta((IPersistentMap)const__50);
        Var var28 = var27;
        var27.bindRoot((Object)new coordination$lookup_endpoint());
        Var var29 = const__51;
        var29.setMeta((IPersistentMap)const__53);
        Var var30 = var29;
        var29.bindRoot((Object)new coordination$lookup_transactor_endpoint());
        Var var31 = const__54;
        var31.setMeta((IPersistentMap)const__56);
        Var var32 = var31;
        var31.bindRoot((Object)new coordination$vc_password());
        Var var33 = const__57;
        var33.setMeta((IPersistentMap)const__59);
        Var var34 = var33;
        var33.bindRoot((Object)new coordination$endpoint__GT_server_spec());
        Var var35 = const__60;
        var35.setMeta((IPersistentMap)const__62);
        Var var36 = var35;
        var35.bindRoot((Object)new coordination$allowed_valcache_client_QMARK_());
        Var var37 = const__63;
        var37.setMeta((IPersistentMap)const__65);
        Var var38 = var37;
        var37.bindRoot((Object)new coordination$check_peer_version());
        Var var39 = const__66;
        var39.setMeta((IPersistentMap)const__68);
        Var var40 = var39;
        var39.bindRoot((Object)new coordination$lookup_compatible_transactor_endpoint());
        Var var41 = const__69;
        var41.setMeta((IPersistentMap)const__71);
        Var var42 = var41;
        var41.bindRoot((Object)new coordination$cluster_conf__GT_resolved_conf());
        Var var43 = const__72;
        var43.setMeta((IPersistentMap)const__73);
        Var var44 = var43;
        var43.bindRoot(((IFn)const__74.getRawRoot()).invoke(((IFn)const__75.getRawRoot()).invoke(const__69.getRawRoot()), ((IFn)const__76.getRawRoot()).invoke(const__77, const__78)));
        Var var45 = const__79;
        var45.setMeta((IPersistentMap)const__81);
        Var var46 = var45;
        var45.bindRoot((Object)new coordination$resolve_db_name());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.coordination");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.coordination", (String)"DEFAULT_HORNET_PORT");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__8 = 4334L;
        const__9 = RT.var((String)"datomic.coordination", (String)"devspec");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"clojure.core", (String)"atom");
        const__12 = RT.var((String)"datomic.coordination", (String)"init-dev");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-map"), (Object)Symbol.intern(null, (String)"data-dir")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.coordination", (String)"init-protocol");
        const__17 = RT.keyword(null, (String)"limited-edition");
        const__18 = RT.keyword(null, (String)"default");
        const__19 = RT.var((String)"datomic.coordination", (String)"create-dev-cluster");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.coordination", (String)"create-cluster");
        const__23 = RT.var((String)"datomic.coordination", (String)"create-db-cluster");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf")))), RT.keyword(null, (String)"column"), 1});
        const__26 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf")))), RT.keyword(null, (String)"column"), 1});
        const__29 = RT.var((String)"datomic.coordination", (String)"pod-key");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.coordination", (String)"standby-key");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__33 = RT.var((String)"datomic.coordination", (String)"heartbeat-keys");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.coordination", (String)"keys-by-role");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__37 = RT.keyword(null, (String)"active");
        const__38 = RT.keyword(null, (String)"standby");
        const__39 = RT.var((String)"datomic.coordination", (String)"PEER_VERSION");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__41 = 2L;
        const__42 = RT.var((String)"datomic.coordination", (String)"create-heartbeat");
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"host"), Symbol.intern(null, (String)"alt-host"), Symbol.intern(null, (String)"port"), Symbol.intern(null, (String)"username"), Symbol.intern(null, (String)"password"), Symbol.intern(null, (String)"version"), Symbol.intern(null, (String)"encrypt-channel"), Symbol.intern(null, (String)"timestamp")})})))), RT.keyword(null, (String)"column"), 1});
        const__45 = RT.var((String)"datomic.coordination", (String)"heartbeat->endpoint");
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"host"), Symbol.intern(null, (String)"alt-host"), Symbol.intern(null, (String)"port"), Symbol.intern(null, (String)"username"), Symbol.intern(null, (String)"password"), Symbol.intern(null, (String)"timestamp"), Symbol.intern(null, (String)"version"), Symbol.intern(null, (String)"encrypt-channel"), Symbol.intern(null, (String)"peer-version")})))), RT.keyword(null, (String)"column"), 1});
        const__48 = RT.var((String)"datomic.coordination", (String)"lookup-endpoint");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"datomic.coordination", (String)"lookup-transactor-endpoint");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.coordination", (String)"vc-password");
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"peer-password")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
        const__57 = RT.var((String)"datomic.coordination", (String)"endpoint->server-spec");
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"endpoint"), (Object)Symbol.intern(null, (String)"repair?")))), RT.keyword(null, (String)"column"), 1});
        const__60 = RT.var((String)"datomic.coordination", (String)"allowed-valcache-client?");
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"server-specs"), (Object)Symbol.intern(null, (String)"client")))), RT.keyword(null, (String)"column"), 1});
        const__63 = RT.var((String)"datomic.coordination", (String)"check-peer-version");
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"endpoint")))), RT.keyword(null, (String)"column"), 1});
        const__66 = RT.var((String)"datomic.coordination", (String)"lookup-compatible-transactor-endpoint");
        const__68 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
        const__69 = RT.var((String)"datomic.coordination", (String)"cluster-conf->resolved-conf");
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf")))), RT.keyword(null, (String)"column"), 1});
        const__72 = RT.var((String)"datomic.coordination", (String)"db-cache");
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__74 = RT.var((String)"datomic.cache", (String)"lookup-cache");
        const__75 = RT.var((String)"datomic.cache", (String)"fn->lookup");
        const__76 = RT.var((String)"datomic.cache", (String)"create-write-limited");
        const__77 = 100L;
        const__78 = 1L;
        const__79 = RT.var((String)"datomic.coordination", (String)"resolve-db-name");
        const__81 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        coordination__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.coordination__init").getClassLoader());
        try {
            coordination__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


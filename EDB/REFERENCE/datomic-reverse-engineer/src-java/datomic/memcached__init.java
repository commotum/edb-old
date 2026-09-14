/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.LockingTransaction
 *  clojure.lang.Numbers
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
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.memcached$configure_auto_discovery;
import datomic.memcached$create_cache;
import datomic.memcached$create_client;
import datomic.memcached$create_recovering_client;
import datomic.memcached$factory_STAR_;
import datomic.memcached$fits_in_memcached_QMARK_;
import datomic.memcached$fn__10012;
import datomic.memcached$fn__10024;
import datomic.memcached$fn__9937;
import datomic.memcached$fn__9968;
import datomic.memcached$fn__9971;
import datomic.memcached$fn__9982;
import datomic.memcached$fn__9999;
import datomic.memcached$loading__6434__auto____9935;
import datomic.memcached$memcached_client_supports_autodiscovery_QMARK_;
import datomic.memcached$op_listener;
import datomic.memcached$put_result_handler;
import datomic.memcached$reify__9950;
import datomic.memcached$safe_deref;
import datomic.memcached$set_client_mode_STAR_;
import datomic.memcached$start_local_memcached_from_config;
import datomic.memcached$start_memcached_from_config;
import datomic.memcached$wrap_metrics;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class memcached__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__10;
    public static final Var const__11;
    public static final AFn const__13;
    public static final Var const__14;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__19;
    public static final Var const__20;
    public static final AFn const__22;
    public static final Var const__23;
    public static final AFn const__25;
    public static final Var const__26;
    public static final AFn const__30;
    public static final Var const__31;
    public static final AFn const__32;
    public static final Object const__33;
    public static final Var const__34;
    public static final AFn const__35;
    public static final Var const__40;
    public static final AFn const__41;
    public static final AFn const__45;
    public static final Var const__46;
    public static final AFn const__48;
    public static final Var const__49;
    public static final AFn const__50;
    public static final Var const__51;
    public static final AFn const__53;
    public static final Var const__54;
    public static final AFn const__56;
    public static final Var const__57;
    public static final AFn const__58;
    public static final AFn const__69;
    public static final Var const__70;
    public static final AFn const__71;
    public static final AFn const__82;
    public static final Object const__83;
    public static final Var const__84;
    public static final Var const__85;
    public static final Var const__86;
    public static final Keyword const__87;
    public static final Var const__88;
    public static final ISeq const__89;
    public static final Var const__90;
    public static final Var const__91;
    public static final AFn const__95;
    public static final Keyword const__96;
    public static final AFn const__97;
    public static final Keyword const__98;
    public static final Keyword const__99;
    public static final AFn const__104;
    public static final Keyword const__105;
    public static final Var const__106;
    public static final Var const__107;
    public static final Var const__108;
    public static final AFn const__109;
    public static final AFn const__110;
    public static final Keyword const__111;
    public static final AFn const__112;
    public static final AFn const__113;
    public static final AFn const__114;
    public static final AFn const__115;
    public static final AFn const__116;
    public static final AFn const__117;
    public static final Var const__118;
    public static final AFn const__119;
    public static final Var const__120;
    public static final AFn const__122;
    public static final Var const__123;
    public static final AFn const__125;
    public static final Var const__126;
    public static final AFn const__128;
    public static final Var const__129;
    public static final AFn const__131;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new memcached$loading__6434__auto____9935()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new memcached$fn__9937())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)new memcached$wrap_metrics());
        Var var3 = const__11;
        var3.setMeta((IPersistentMap)const__13);
        Var var4 = var3;
        var3.bindRoot((Object)new memcached$safe_deref());
        Var var5 = const__14;
        var5.setMeta((IPersistentMap)const__16);
        Var var6 = var5;
        var5.bindRoot((Object)new memcached$put_result_handler());
        Var var7 = const__17;
        var7.setMeta((IPersistentMap)const__19);
        Var var8 = var7;
        var7.bindRoot((Object)new memcached$op_listener());
        Var var9 = const__20;
        var9.setMeta((IPersistentMap)const__22);
        Var var10 = var9;
        var9.bindRoot((Object)new memcached$memcached_client_supports_autodiscovery_QMARK_());
        Var var11 = const__23;
        var11.setMeta((IPersistentMap)const__25);
        Var var12 = var11;
        var11.bindRoot((Object)new memcached$set_client_mode_STAR_());
        Var var13 = const__26;
        var13.setMeta((IPersistentMap)const__30);
        Var var14 = var13;
        var13.bindRoot((Object)new memcached$configure_auto_discovery());
        Var var15 = const__31;
        var15.setMeta((IPersistentMap)const__32);
        Var var16 = var15;
        var15.bindRoot(const__33);
        Var var17 = const__34;
        var17.setMeta((IPersistentMap)const__35);
        Var var18 = var17;
        var17.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)Numbers.multiply((long)20L, (long)1024L), (long)1024L)));
        Var var19 = const__40;
        var19.setMeta((IPersistentMap)const__41);
        Var var20 = var19;
        var19.bindRoot((Object)((IObj)new memcached$reify__9950(null)).withMeta((IPersistentMap)const__45));
        Var var21 = const__46;
        var21.setMeta((IPersistentMap)const__48);
        Var var22 = var21;
        var21.bindRoot((Object)new memcached$factory_STAR_());
        Var var23 = const__49;
        var23.setMeta((IPersistentMap)const__50);
        Var var24 = var23;
        var23.bindRoot(const__46.getRawRoot());
        Var var25 = const__51;
        var25.setMeta((IPersistentMap)const__53);
        Var var26 = var25;
        var25.bindRoot((Object)new memcached$create_client());
        Var var27 = const__54;
        var27.setMeta((IPersistentMap)const__56);
        Var var28 = var27;
        var27.bindRoot((Object)new memcached$fits_in_memcached_QMARK_());
        Var var29 = const__57;
        var29.setMeta((IPersistentMap)const__58);
        Var var30 = var29;
        var29.bindRoot((Object)const__69);
        Var var31 = const__70;
        var31.setMeta((IPersistentMap)const__71);
        Var var32 = var31;
        var31.bindRoot((Object)const__82);
        Object object4 = ((IFn)new memcached$fn__9968()).invoke();
        Object object5 = const__83;
        Object object6 = ((IFn)const__84.getRawRoot()).invoke((Object)const__85, const__86.getRawRoot(), (Object)const__87, null);
        Object object7 = ((IFn)const__88).invoke((Object)const__85, (Object)const__89);
        Object object8 = ((IFn)const__90.getRawRoot()).invoke((Object)const__85, const__91.getRawRoot(), ((IFn)const__86.getRawRoot()).invoke((Object)const__95, (Object)const__96, (Object)const__97, (Object)const__98, (Object)const__85, (Object)const__99, (Object)const__104, (Object)const__105, (Object)RT.map((Object[])new Object[]{((IFn)const__106.getRawRoot()).invoke(const__107.get(), ((IFn)const__108.getRawRoot()).invoke((Object)const__109, ((IFn)const__91.getRawRoot()).invoke((Object)const__110, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__111, const__85})))), new memcached$fn__9971(), ((IFn)const__106.getRawRoot()).invoke(const__107.get(), ((IFn)const__108.getRawRoot()).invoke((Object)const__112, ((IFn)const__91.getRawRoot()).invoke((Object)const__113, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__111, const__85})))), new memcached$fn__9982(), ((IFn)const__106.getRawRoot()).invoke(const__107.get(), ((IFn)const__108.getRawRoot()).invoke((Object)const__114, ((IFn)const__91.getRawRoot()).invoke((Object)const__115, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__111, const__85})))), new memcached$fn__9999(), ((IFn)const__106.getRawRoot()).invoke(const__107.get(), ((IFn)const__108.getRawRoot()).invoke((Object)const__116, ((IFn)const__91.getRawRoot()).invoke((Object)const__117, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__111, const__85})))), new memcached$fn__10012()})));
        Object object9 = ((IFn)const__118.getRawRoot()).invoke(const__85.getRawRoot());
        AFn aFn = const__119;
        Object object10 = ((IFn)new memcached$fn__10024()).invoke();
        Var var33 = const__120;
        var33.setMeta((IPersistentMap)const__122);
        Var var34 = var33;
        var33.bindRoot((Object)new memcached$create_recovering_client());
        Var var35 = const__123;
        var35.setMeta((IPersistentMap)const__125);
        Var var36 = var35;
        var35.bindRoot((Object)new memcached$create_cache());
        Var var37 = const__126;
        var37.setMeta((IPersistentMap)const__128);
        Var var38 = var37;
        var37.bindRoot((Object)new memcached$start_local_memcached_from_config());
        Var var39 = const__129;
        var39.setMeta((IPersistentMap)const__131);
        Var var40 = var39;
        var39.bindRoot((Object)new memcached$start_memcached_from_config());
        Object v52 = null;
        Object v53 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.memcached");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.memcached", (String)"wrap-metrics");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"record-kv"), (Object)Symbol.intern(null, (String)"succ"), (Object)Symbol.intern(null, (String)"fail")))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.memcached", (String)"safe-deref");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"fut")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Future")}))))), RT.keyword(null, (String)"column"), 1});
        const__14 = RT.var((String)"datomic.memcached", (String)"put-result-handler");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"fut")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.memcached", (String)"op-listener");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"f"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"datomic.spy.memcached.internal.OperationCompletionListener")})))), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.memcached", (String)"memcached-client-supports-autodiscovery?");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.memcached", (String)"set-client-mode*");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"builder")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ConnectionFactoryBuilder")})), (Object)Symbol.intern(null, (String)"auto-discovery"), (Object)Symbol.intern(null, (String)"config-timeout-msec")))), RT.keyword(null, (String)"column"), 1});
        const__26 = RT.var((String)"datomic.memcached", (String)"configure-auto-discovery");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.spy.memcached.ConnectionFactoryBuilder"), RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"builder")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ConnectionFactoryBuilder")})), (Object)Symbol.intern(null, (String)"auto-discovery"), (Object)Symbol.intern(null, (String)"config-timeout-msec")))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.memcached", (String)"SPY_BYTEARRAY_FLAGS");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__33 = 2048L;
        const__34 = RT.var((String)"datomic.memcached", (String)"SPY_MAX_SIZE");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__40 = RT.var((String)"datomic.memcached", (String)"legacy-transcoder");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 94, RT.keyword(null, (String)"column"), 3});
        const__46 = RT.var((String)"datomic.memcached", (String)"factory*");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"timeout-msec"), (Object)Symbol.intern(null, (String)"config-timeout-msec"), (Object)Symbol.intern(null, (String)"username"), (Object)Symbol.intern(null, (String)"password"), (Object)Symbol.intern(null, (String)"auto-discovery")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"timeout-msec"), 10L, Symbol.intern(null, (String)"config-timeout-msec"), 100L})})))), RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"datomic.memcached", (String)"factory");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"datomic.memcached", (String)"create-client");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"servers")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"args")})))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.memcached", (String)"fits-in-memcached?");
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"v")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ByteBuffer")}))))), RT.keyword(null, (String)"column"), 1});
        const__57 = RT.var((String)"datomic.memcached", (String)"local-memcached-metric-names");
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__69 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"local-memcached"), RT.keyword(null, (String)"local-memcached-ns"), RT.keyword(null, (String)"LocalMemcache"), RT.keyword(null, (String)"LocalMemcachedGetSucceededNsec"), RT.keyword(null, (String)"LocalMemcachedGetFailedNsec"), RT.keyword(null, (String)"LocalMemcachedGetMissedNsec"), RT.keyword(null, (String)"LocalMemcachedGetTimeoutNsec"), RT.keyword(null, (String)"LocalMemcachedGetQueueFullNsec"), RT.keyword(null, (String)"LocalMemcachedPutSucceededNsec"), RT.keyword(null, (String)"LocalMemcachedPutFailedNsec")});
        const__70 = RT.var((String)"datomic.memcached", (String)"memcached-metric-names");
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__82 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"memcached"), RT.keyword(null, (String)"memcached-ns"), RT.keyword(null, (String)"Memcache"), RT.keyword(null, (String)"MemcachedGetSucceededNsec"), RT.keyword(null, (String)"MemcachedGetFailedNsec"), RT.keyword(null, (String)"MemcachedGetMissedNsec"), RT.keyword(null, (String)"MemcachedGetTimeoutNsec"), RT.keyword(null, (String)"MemcachedGetQueueFullNsec"), RT.keyword(null, (String)"MemcachedPutSucceededNsec"), RT.keyword(null, (String)"MemcachedPutFailedNsec")});
        const__83 = RT.classForName((String)"datomic.memcached.RecoveringClientImpl");
        const__84 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__85 = RT.var((String)"datomic.memcached", (String)"RecoveringClientImpl");
        const__86 = RT.var((String)"clojure.core", (String)"assoc");
        const__87 = RT.keyword(null, (String)"doc");
        const__88 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__89 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"rc-shutdown")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), ((IObj)Symbol.intern(null, (String)"rc-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))})), ((IObj)Symbol.intern(null, (String)"rc-set")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"ttl"), (Object)Symbol.intern(null, (String)"v"))))})), ((IObj)Symbol.intern(null, (String)"rc-reset-if-crashed")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__90 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__91 = RT.var((String)"clojure.core", (String)"merge");
        const__95 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.memcached.RecoveringClientImpl"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.memcached.RecoveringClientImpl")});
        const__96 = RT.keyword(null, (String)"sigs");
        const__97 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"rc-shutdown"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"rc-shutdown")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), null}), RT.keyword(null, (String)"rc-get"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"rc-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), null}), RT.keyword(null, (String)"rc-set"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"OperationFuture"), RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"rc-set")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"ttl"), (Object)Symbol.intern(null, (String)"v"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"ttl"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"doc"), null}), RT.keyword(null, (String)"rc-reset-if-crashed"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"rc-reset-if-crashed")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), null})});
        const__98 = RT.keyword(null, (String)"var");
        const__99 = RT.keyword(null, (String)"method-map");
    }

    public static void __init1() {
        const__104 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"rc-reset-if-crashed"), RT.keyword(null, (String)"rc-reset-if-crashed"), RT.keyword(null, (String)"rc-shutdown"), RT.keyword(null, (String)"rc-shutdown"), RT.keyword(null, (String)"rc-set"), RT.keyword(null, (String)"rc-set"), RT.keyword(null, (String)"rc-get"), RT.keyword(null, (String)"rc-get")});
        const__105 = RT.keyword(null, (String)"method-builders");
        const__106 = RT.var((String)"clojure.core", (String)"intern");
        const__107 = RT.var((String)"clojure.core", (String)"*ns*");
        const__108 = RT.var((String)"clojure.core", (String)"with-meta");
        const__109 = (AFn)((IObj)Symbol.intern(null, (String)"rc-reset-if-crashed")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__110 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"rc-reset-if-crashed")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), null});
        const__111 = RT.keyword(null, (String)"protocol");
        const__112 = (AFn)((IObj)Symbol.intern(null, (String)"rc-set")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"ttl"), (Object)Symbol.intern(null, (String)"v"))))}));
        const__113 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"OperationFuture"), RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"rc-set")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"ttl"), (Object)Symbol.intern(null, (String)"v"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"ttl"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"doc"), null});
        const__114 = (AFn)((IObj)Symbol.intern(null, (String)"rc-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))}));
        const__115 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"rc-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), null});
        const__116 = (AFn)((IObj)Symbol.intern(null, (String)"rc-shutdown")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__117 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"rc-shutdown")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), null});
        const__118 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__119 = (AFn)Symbol.intern(null, (String)"RecoveringClientImpl");
        const__120 = RT.var((String)"datomic.memcached", (String)"create-recovering-client");
        const__122 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"create-client"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"datomic.memcached.RecoveringClient")})))), RT.keyword(null, (String)"column"), 1});
        const__123 = RT.var((String)"datomic.memcached", (String)"create-cache");
        const__125 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"shutdown-client?"), (Object)Symbol.intern(null, (String)"record-kv"), (Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"metric-names")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"shutdown-client?"), Boolean.TRUE, Symbol.intern(null, (String)"record-kv"), Symbol.intern((String)"monitor", (String)"add-stat"), Symbol.intern(null, (String)"metric-names"), Symbol.intern(null, (String)"memcached-metric-names")}), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"args")}))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.AutoCloseable")})))), RT.keyword(null, (String)"column"), 1});
        const__126 = RT.var((String)"datomic.memcached", (String)"start-local-memcached-from-config");
        const__128 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__129 = RT.var((String)"datomic.memcached", (String)"start-memcached-from-config");
        const__131 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
    }

    static {
        memcached__init.__init0();
        memcached__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.memcached__init").getClassLoader());
        try {
            memcached__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


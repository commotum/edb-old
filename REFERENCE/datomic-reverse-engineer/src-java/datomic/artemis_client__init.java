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
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.artemis_client$create_connector;
import datomic.artemis_client$create_consumer;
import datomic.artemis_client$create_deserializer;
import datomic.artemis_client$create_fressian_message;
import datomic.artemis_client$create_message;
import datomic.artemis_client$create_producer;
import datomic.artemis_client$create_rpc_client;
import datomic.artemis_client$create_rpc_server;
import datomic.artemis_client$create_serializer;
import datomic.artemis_client$create_server_locator;
import datomic.artemis_client$create_session_factory;
import datomic.artemis_client$create_temporary_queue;
import datomic.artemis_client$create_transport;
import datomic.artemis_client$delete_queue;
import datomic.artemis_client$fn__20770;
import datomic.artemis_client$fn__20782;
import datomic.artemis_client$fn__20785;
import datomic.artemis_client$fn__20805;
import datomic.artemis_client$fn__20831;
import datomic.artemis_client$fn__20833;
import datomic.artemis_client$fn__20835;
import datomic.artemis_client$fn__20837;
import datomic.artemis_client$fn__20839;
import datomic.artemis_client$fn__20847;
import datomic.artemis_client$fn__20870;
import datomic.artemis_client$fn__20911;
import datomic.artemis_client$fressian_producer;
import datomic.artemis_client$input_stream;
import datomic.artemis_client$loading__6434__auto____20768;
import datomic.artemis_client$output_stream;
import datomic.artemis_client$read_batch;
import datomic.artemis_client$rpc_request;
import datomic.artemis_client$set_handler;
import datomic.artemis_client$start_session;
import datomic.artemis_client$wrap_as_failure_listener;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class artemis_client__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__7;
    public static final Var const__8;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__15;
    public static final Var const__16;
    public static final AFn const__18;
    public static final Object const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final ISeq const__25;
    public static final Var const__26;
    public static final Var const__27;
    public static final AFn const__31;
    public static final Keyword const__32;
    public static final AFn const__33;
    public static final Keyword const__34;
    public static final Keyword const__35;
    public static final AFn const__37;
    public static final Keyword const__38;
    public static final Var const__39;
    public static final Var const__40;
    public static final Var const__41;
    public static final AFn const__42;
    public static final AFn const__43;
    public static final Keyword const__44;
    public static final Var const__45;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__53;
    public static final Var const__54;
    public static final Object const__55;
    public static final AFn const__57;
    public static final Var const__58;
    public static final AFn const__61;
    public static final Var const__62;
    public static final Object const__63;
    public static final Var const__64;
    public static final Keyword const__65;
    public static final Object const__66;
    public static final Object const__67;
    public static final Object const__68;
    public static final Var const__69;
    public static final AFn const__71;
    public static final Var const__72;
    public static final AFn const__74;
    public static final Var const__75;
    public static final AFn const__77;
    public static final Var const__78;
    public static final AFn const__80;
    public static final Var const__81;
    public static final Keyword const__82;
    public static final Var const__83;
    public static final AFn const__85;
    public static final Var const__86;
    public static final AFn const__89;
    public static final Var const__90;
    public static final AFn const__92;
    public static final Var const__93;
    public static final AFn const__95;
    public static final Var const__96;
    public static final AFn const__98;
    public static final Var const__99;
    public static final AFn const__101;
    public static final Var const__102;
    public static final AFn const__104;
    public static final Var const__105;
    public static final AFn const__107;
    public static final Var const__108;
    public static final AFn const__110;
    public static final Var const__111;
    public static final AFn const__113;
    public static final Var const__114;
    public static final AFn const__116;
    public static final Var const__117;
    public static final AFn const__119;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new artemis_client$loading__6434__auto____20768()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new artemis_client$fn__20770())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot((Object)"org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory");
        Var var3 = const__8;
        var3.setMeta((IPersistentMap)const__9);
        Var var4 = var3;
        var3.bindRoot((Object)"org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory");
        Var var5 = const__10;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new artemis_client$create_transport());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new artemis_client$create_connector());
        Object object4 = ((IFn)new artemis_client$fn__20782()).invoke();
        Object object5 = const__19;
        Object object6 = ((IFn)const__20.getRawRoot()).invoke((Object)const__21, const__22.getRawRoot(), (Object)const__23, null);
        Object object7 = ((IFn)const__24).invoke((Object)const__21, (Object)const__25);
        Object object8 = ((IFn)const__26.getRawRoot()).invoke((Object)const__21, const__27.getRawRoot(), ((IFn)const__22.getRawRoot()).invoke((Object)const__31, (Object)const__32, (Object)const__33, (Object)const__34, (Object)const__21, (Object)const__35, (Object)const__37, (Object)const__38, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__39.getRawRoot()).invoke(const__40.get(), ((IFn)const__41.getRawRoot()).invoke((Object)const__42, ((IFn)const__27.getRawRoot()).invoke((Object)const__43, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__44, const__21})))), new artemis_client$fn__20785()})));
        Object object9 = ((IFn)const__45.getRawRoot()).invoke(const__21.getRawRoot());
        AFn aFn = const__46;
        Var var9 = const__47;
        var9.setMeta((IPersistentMap)const__49);
        Var var10 = var9;
        var9.bindRoot((Object)new artemis_client$start_session());
        Var var11 = const__50;
        var11.setMeta((IPersistentMap)const__53);
        Var var12 = var11;
        var11.bindRoot((Object)new artemis_client$wrap_as_failure_listener());
        Object object10 = ((IFn)new artemis_client$fn__20805()).invoke();
        Var var13 = const__54;
        var13.setMeta((IPersistentMap)const__57);
        Var var14 = var13;
        var13.bindRoot((Object)new artemis_client$create_server_locator());
        Var var15 = const__58;
        var15.setMeta((IPersistentMap)const__61);
        Var var16 = var15;
        var15.bindRoot((Object)new artemis_client$create_session_factory());
        Object object11 = ((IFn)const__62.getRawRoot()).invoke(const__63, const__64.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__65, new artemis_client$fn__20831()}));
        Object object12 = ((IFn)const__62.getRawRoot()).invoke(const__66, const__64.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__65, new artemis_client$fn__20833()}));
        Object object13 = ((IFn)const__62.getRawRoot()).invoke(const__67, const__64.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__65, new artemis_client$fn__20835()}));
        Object object14 = ((IFn)const__62.getRawRoot()).invoke(const__68, const__64.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__65, new artemis_client$fn__20837()}));
        Object object15 = ((IFn)const__62.getRawRoot()).invoke(const__55, const__64.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__65, new artemis_client$fn__20839()}));
        Var var17 = const__69;
        var17.setMeta((IPersistentMap)const__71);
        Var var18 = var17;
        var17.bindRoot((Object)new artemis_client$create_temporary_queue());
        Var var19 = const__72;
        var19.setMeta((IPersistentMap)const__74);
        Var var20 = var19;
        var19.bindRoot((Object)new artemis_client$delete_queue());
        Var var21 = const__75;
        var21.setMeta((IPersistentMap)const__77);
        Var var22 = var21;
        var21.bindRoot((Object)new artemis_client$create_producer());
        Var var23 = const__78;
        var23.setMeta((IPersistentMap)const__80);
        Var var24 = var23;
        var23.bindRoot((Object)new artemis_client$create_consumer());
        Object object16 = ((IFn)const__62.getRawRoot()).invoke(const__67, const__81.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__82, new artemis_client$fn__20847()}));
        Var var25 = const__83;
        var25.setMeta((IPersistentMap)const__85);
        Var var26 = var25;
        var25.bindRoot((Object)new artemis_client$set_handler());
        Var var27 = const__86;
        var27.setMeta((IPersistentMap)const__89);
        Var var28 = var27;
        var27.bindRoot((Object)new artemis_client$create_message());
        Var var29 = const__90;
        var29.setMeta((IPersistentMap)const__92);
        Var var30 = var29;
        var29.bindRoot((Object)new artemis_client$output_stream());
        Var var31 = const__93;
        var31.setMeta((IPersistentMap)const__95);
        Var var32 = var31;
        var31.bindRoot((Object)new artemis_client$input_stream());
        Var var33 = const__96;
        var33.setMeta((IPersistentMap)const__98);
        Var var34 = var33;
        var33.bindRoot((Object)new artemis_client$create_fressian_message());
        Var var35 = const__99;
        var35.setMeta((IPersistentMap)const__101);
        Var var36 = var35;
        var35.bindRoot((Object)new artemis_client$create_serializer());
        Var var37 = const__102;
        var37.setMeta((IPersistentMap)const__104);
        Var var38 = var37;
        var37.bindRoot((Object)new artemis_client$create_deserializer());
        Var var39 = const__105;
        var39.setMeta((IPersistentMap)const__107);
        Var var40 = var39;
        var39.bindRoot((Object)new artemis_client$read_batch());
        Var var41 = const__108;
        var41.setMeta((IPersistentMap)const__110);
        Var var42 = var41;
        var41.bindRoot((Object)new artemis_client$fressian_producer());
        Object object17 = ((IFn)new artemis_client$fn__20870()).invoke();
        Var var43 = const__111;
        var43.setMeta((IPersistentMap)const__113);
        Var var44 = var43;
        var43.bindRoot((Object)new artemis_client$create_rpc_client());
        Object object18 = ((IFn)new artemis_client$fn__20911()).invoke();
        Var var45 = const__114;
        var45.setMeta((IPersistentMap)const__116);
        Var var46 = var45;
        var45.bindRoot((Object)new artemis_client$create_rpc_server());
        Var var47 = const__117;
        var47.setMeta((IPersistentMap)const__119);
        Var var48 = var47;
        var47.bindRoot((Object)new artemis_client$rpc_request());
        Object v68 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.artemis-client");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.artemis-client", (String)"netty-connector-factory");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__8 = RT.var((String)"datomic.artemis-client", (String)"in-vm-connector-factory");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.artemis-client", (String)"create-transport");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.apache.activemq.artemis.api.core.TransportConfiguration"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"factory"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"transport-opts")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.artemis-client", (String)"create-connector");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.apache.activemq.artemis.api.core.TransportConfiguration"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"connector-class-name"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"kvs")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.classForName((String)"datomic.artemis_client.HornetImpl");
        const__20 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__21 = RT.var((String)"datomic.artemis-client", (String)"HornetImpl");
        const__22 = RT.var((String)"clojure.core", (String)"assoc");
        const__23 = RT.keyword(null, (String)"doc");
        const__24 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__25 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"start-session*")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"args"))))}))));
        const__26 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__27 = RT.var((String)"clojure.core", (String)"merge");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.artemis_client.HornetImpl"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.artemis_client.HornetImpl")});
        const__32 = RT.keyword(null, (String)"sigs");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"start-session*"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"start-session*")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"args"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"doc"), null})});
        const__34 = RT.keyword(null, (String)"var");
        const__35 = RT.keyword(null, (String)"method-map");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"start-session*"), RT.keyword(null, (String)"start-session*")});
        const__38 = RT.keyword(null, (String)"method-builders");
        const__39 = RT.var((String)"clojure.core", (String)"intern");
        const__40 = RT.var((String)"clojure.core", (String)"*ns*");
        const__41 = RT.var((String)"clojure.core", (String)"with-meta");
        const__42 = (AFn)((IObj)Symbol.intern(null, (String)"start-session*")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"args"))))}));
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"start-session*")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"args"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"doc"), null});
        const__44 = RT.keyword(null, (String)"protocol");
        const__45 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__46 = (AFn)Symbol.intern(null, (String)"HornetImpl");
        const__47 = RT.var((String)"datomic.artemis-client", (String)"start-session");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"factory"), (Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.artemis-client", (String)"wrap-as-failure-listener");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.artemis-client", (String)"create-server-locator");
        const__55 = RT.classForName((String)"org.apache.activemq.artemis.api.core.client.ServerLocator");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.apache.activemq.artemis.api.core.client.ServerLocator"), RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"connector"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"ttl"))})))), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.artemis-client", (String)"create-session-factory");
        const__61 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.artemis_client.SessionFactoryBundle"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"connector")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"TransportConfiguration")})), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"column"), 1});
        const__62 = RT.var((String)"clojure.core", (String)"extend");
        const__63 = RT.classForName((String)"org.apache.activemq.artemis.api.core.client.ClientProducer");
        const__64 = RT.var((String)"datomic.common", (String)"AsyncShutdown");
        const__65 = RT.keyword(null, (String)"async-shutdown");
        const__66 = RT.classForName((String)"org.apache.activemq.artemis.api.core.client.ClientSessionFactory");
        const__67 = RT.classForName((String)"org.apache.activemq.artemis.api.core.client.ClientConsumer");
        const__68 = RT.classForName((String)"org.apache.activemq.artemis.api.core.client.ClientSession");
        const__69 = RT.var((String)"datomic.artemis-client", (String)"create-temporary-queue");
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientSession")})), (Object)((IObj)Symbol.intern(null, (String)"address")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
        const__72 = RT.var((String)"datomic.artemis-client", (String)"delete-queue");
        const__74 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientSession")})), (Object)((IObj)Symbol.intern(null, (String)"queue")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
        const__75 = RT.var((String)"datomic.artemis-client", (String)"create-producer");
        const__77 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.apache.activemq.artemis.api.core.client.ClientProducer"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientSession")})), (Object)((IObj)Symbol.intern(null, (String)"address")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
        const__78 = RT.var((String)"datomic.artemis-client", (String)"create-consumer");
        const__80 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.apache.activemq.artemis.api.core.client.ClientConsumer"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientSession")})), (Object)((IObj)Symbol.intern(null, (String)"queue-name")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"window-size"), (Object)Symbol.intern(null, (String)"max-rate"), (Object)Symbol.intern(null, (String)"browse")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"window-size"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"*"), 1024L, 1024L))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 66})), Symbol.intern(null, (String)"max-rate"), -1L, Symbol.intern(null, (String)"browse"), Boolean.FALSE})})))), RT.keyword(null, (String)"column"), 1});
        const__81 = RT.var((String)"datomic.queue", (String)"BlockingConsumer");
        const__82 = RT.keyword(null, (String)"take");
        const__83 = RT.var((String)"datomic.artemis-client", (String)"set-handler");
        const__85 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"consumer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientConsumer")})), (Object)Symbol.intern(null, (String)"handler")))), RT.keyword(null, (String)"column"), 1});
        const__86 = RT.var((String)"datomic.artemis-client", (String)"create-message");
        const__89 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.apache.activemq.artemis.api.core.client.ClientMessage"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientSession")})), (Object)Symbol.intern(null, (String)"durable")), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientSession")})), (Object)Symbol.intern(null, (String)"message-type"), (Object)Symbol.intern(null, (String)"durable")), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"session")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientSession")})), (Object)Symbol.intern(null, (String)"message-type"), (Object)Symbol.intern(null, (String)"durable"), (Object)Symbol.intern(null, (String)"expiration"), (Object)Symbol.intern(null, (String)"timestamp"), (Object)Symbol.intern(null, (String)"priority")))), RT.keyword(null, (String)"column"), 1});
        const__90 = RT.var((String)"datomic.artemis-client", (String)"output-stream");
        const__92 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"msg")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientMessage")}))))), RT.keyword(null, (String)"column"), 1});
        const__93 = RT.var((String)"datomic.artemis-client", (String)"input-stream");
        const__95 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"msg")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientMessage")}))))), RT.keyword(null, (String)"column"), 1});
        const__96 = RT.var((String)"datomic.artemis-client", (String)"create-fressian-message");
        const__98 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"session"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"obj"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"message-args")))), RT.keyword(null, (String)"column"), 1});
        const__99 = RT.var((String)"datomic.artemis-client", (String)"create-serializer");
    }

    public static void __init1() {
        const__101 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"write-handlers")))), RT.keyword(null, (String)"column"), 1});
        const__102 = RT.var((String)"datomic.artemis-client", (String)"create-deserializer");
        const__104 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"read-handlers")))), RT.keyword(null, (String)"column"), 1});
        const__105 = RT.var((String)"datomic.artemis-client", (String)"read-batch");
        const__107 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"read-handlers")))), RT.keyword(null, (String)"column"), 1});
        const__108 = RT.var((String)"datomic.artemis-client", (String)"fressian-producer");
        const__110 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"hornet-session"), (Object)((IObj)Symbol.intern(null, (String)"hornet-producer")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientProducer")})), (Object)Symbol.intern(null, (String)"write-handlers")))), RT.keyword(null, (String)"column"), 1});
        const__111 = RT.var((String)"datomic.artemis-client", (String)"create-rpc-client");
        const__113 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"session"), (Object)Symbol.intern(null, (String)"request-address"), (Object)Symbol.intern(null, (String)"response-address"), (Object)Symbol.intern(null, (String)"&"), (Object)Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"read-handlers"), (Object)Symbol.intern(null, (String)"write-handlers"))}))))), RT.keyword(null, (String)"column"), 1});
        const__114 = RT.var((String)"datomic.artemis-client", (String)"create-rpc-server");
        const__116 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"session-fn"), (Object)Symbol.intern(null, (String)"request-address"), (Object)Symbol.intern(null, (String)"response-address"), (Object)Symbol.intern(null, (String)"handler"), (Object)Symbol.intern(null, (String)"&"), (Object)Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"read-handlers"), (Object)Symbol.intern(null, (String)"write-handlers"))}))))), RT.keyword(null, (String)"column"), 1});
        const__117 = RT.var((String)"datomic.artemis-client", (String)"rpc-request");
        const__119 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"conn")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"RpcClient")})), (Object)Symbol.intern(null, (String)"request")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        artemis_client__init.__init0();
        artemis_client__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.artemis_client__init").getClassLoader());
        try {
            artemis_client__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


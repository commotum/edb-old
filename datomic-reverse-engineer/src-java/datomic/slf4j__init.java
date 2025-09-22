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
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.MDC
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
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.slf4j$caused_by;
import datomic.slf4j$debug;
import datomic.slf4j$dont_log_time;
import datomic.slf4j$enabled_method;
import datomic.slf4j$error;
import datomic.slf4j$exception_string;
import datomic.slf4j$fn__8962;
import datomic.slf4j$fn__8989;
import datomic.slf4j$fn__8994;
import datomic.slf4j$fn__9000;
import datomic.slf4j$fn__9005;
import datomic.slf4j$fn__9009;
import datomic.slf4j$format_as_msec;
import datomic.slf4j$format_as_musec;
import datomic.slf4j$info;
import datomic.slf4j$loading__6434__auto____8960;
import datomic.slf4j$log_agent_error;
import datomic.slf4j$log_expr;
import datomic.slf4j$log_time;
import datomic.slf4j$log_uncaught_exceptions;
import datomic.slf4j$metric_expr;
import datomic.slf4j$print_and_warn;
import datomic.slf4j$print_safely;
import datomic.slf4j$process;
import datomic.slf4j$sanitize_uri;
import datomic.slf4j$trace;
import datomic.slf4j$warn;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import org.slf4j.MDC;

public class slf4j__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Object const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__15;
    public static final Var const__16;
    public static final AFn const__17;
    public static final AFn const__62;
    public static final Var const__63;
    public static final AFn const__65;
    public static final Var const__66;
    public static final AFn const__68;
    public static final Var const__69;
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
    public static final Var const__88;
    public static final AFn const__90;
    public static final Var const__91;
    public static final AFn const__93;
    public static final Var const__94;
    public static final AFn const__96;
    public static final Var const__97;
    public static final AFn const__99;
    public static final Var const__100;
    public static final AFn const__102;
    public static final Var const__103;
    public static final AFn const__105;
    public static final Var const__106;
    public static final AFn const__107;
    public static final AFn const__108;
    public static final Var const__109;
    public static final AFn const__111;
    public static final Var const__112;
    public static final AFn const__114;
    public static final Var const__115;
    public static final AFn const__117;
    public static final Var const__118;
    public static final AFn const__120;
    public static final Var const__121;
    public static final Var const__122;
    public static final Var const__123;
    public static final AFn const__125;
    public static final Var const__126;
    public static final Object const__127;
    public static final Object const__128;
    public static final Keyword const__129;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new slf4j$loading__6434__auto____8960()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new slf4j$fn__8962())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot(((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke((Object)ManagementFactory.getRuntimeMXBean().getName(), const__10, (Object)"")));
        String string = System.getProperty("net.spy.log.LoggerImpl");
        String string2 = string != null && string != Boolean.FALSE ? null : System.setProperty("net.spy.log.LoggerImpl", "datomic.spy.memcached.compat.log.Log4JLogger");
        MDC.put((String)"pid", (String)((String)((IFn)const__11.getRawRoot()).invoke(const__4.getRawRoot())));
        Object v8 = null;
        Var var3 = const__12;
        var3.setMeta((IPersistentMap)const__15);
        Var var4 = var3;
        var3.bindRoot((Object)new slf4j$print_safely());
        Var var5 = const__16;
        var5.setMeta((IPersistentMap)const__17);
        Var var6 = var5;
        var5.bindRoot((Object)const__62);
        Var var7 = const__63;
        var7.setMeta((IPersistentMap)const__65);
        Var var8 = var7;
        var7.bindRoot((Object)new slf4j$process());
        Var var9 = const__66;
        var9.setMeta((IPersistentMap)const__68);
        Var var10 = var9;
        var9.bindRoot((Object)new slf4j$caused_by());
        Var var11 = const__69;
        var11.setMeta((IPersistentMap)const__72);
        Var var12 = var11;
        var11.bindRoot((Object)new slf4j$enabled_method());
        Var var13 = const__73;
        var13.setMeta((IPersistentMap)const__75);
        Var var14 = var13;
        var13.bindRoot((Object)new slf4j$log_expr());
        Var var15 = const__76;
        var15.setMeta((IPersistentMap)const__78);
        Var var16 = var15;
        var15.bindRoot((Object)new slf4j$trace());
        const__76.setMacro();
        Object v23 = null;
        Var var17 = const__76;
        Var var18 = const__79;
        var18.setMeta((IPersistentMap)const__81);
        Var var19 = var18;
        var18.bindRoot((Object)new slf4j$debug());
        const__79.setMacro();
        Object v27 = null;
        Var var20 = const__79;
        Var var21 = const__82;
        var21.setMeta((IPersistentMap)const__84);
        Var var22 = var21;
        var21.bindRoot((Object)new slf4j$info());
        const__82.setMacro();
        Object v31 = null;
        Var var23 = const__82;
        Var var24 = const__85;
        var24.setMeta((IPersistentMap)const__87);
        Var var25 = var24;
        var24.bindRoot((Object)new slf4j$warn());
        const__85.setMacro();
        Object v35 = null;
        Var var26 = const__85;
        Var var27 = const__88;
        var27.setMeta((IPersistentMap)const__90);
        Var var28 = var27;
        var27.bindRoot((Object)new slf4j$error());
        const__88.setMacro();
        Object v39 = null;
        Var var29 = const__88;
        Var var30 = const__91;
        var30.setMeta((IPersistentMap)const__93);
        Var var31 = var30;
        var30.bindRoot((Object)new slf4j$exception_string());
        Var var32 = const__94;
        var32.setMeta((IPersistentMap)const__96);
        Var var33 = var32;
        var32.bindRoot((Object)new slf4j$log_agent_error());
        Var var34 = const__97;
        var34.setMeta((IPersistentMap)const__99);
        Var var35 = var34;
        var34.bindRoot((Object)new slf4j$format_as_msec());
        Var var36 = const__100;
        var36.setMeta((IPersistentMap)const__102);
        Var var37 = var36;
        var36.bindRoot((Object)new slf4j$format_as_musec());
        Var var38 = const__103;
        var38.setMeta((IPersistentMap)const__105);
        Var var39 = var38;
        var38.bindRoot((Object)new slf4j$metric_expr());
        Var var40 = const__106;
        var40.setMeta((IPersistentMap)const__107);
        Var var41 = var40;
        var40.bindRoot((Object)const__108);
        Var var42 = const__109;
        var42.setMeta((IPersistentMap)const__111);
        Var var43 = var42;
        var42.bindRoot((Object)new slf4j$log_time());
        const__109.setMacro();
        Object v55 = null;
        Var var44 = const__109;
        Var var45 = const__112;
        var45.setMeta((IPersistentMap)const__114);
        Var var46 = var45;
        var45.bindRoot((Object)new slf4j$dont_log_time());
        const__112.setMacro();
        Object v59 = null;
        Var var47 = const__112;
        Var var48 = const__115;
        var48.setMeta((IPersistentMap)const__117);
        Var var49 = var48;
        var48.bindRoot((Object)new slf4j$sanitize_uri());
        Var var50 = const__118;
        var50.setMeta((IPersistentMap)const__120);
        Var var51 = var50;
        var50.bindRoot((Object)new slf4j$print_and_warn());
        Object object4 = ((IFn)const__121.getRawRoot()).invoke(const__122.getRawRoot(), (Object)new slf4j$fn__8989());
        Var var52 = const__123;
        var52.setMeta((IPersistentMap)const__125);
        Var var53 = var52;
        var52.bindRoot((Object)new slf4j$log_uncaught_exceptions());
        Object object5 = ((IFn)new slf4j$fn__8994()).invoke();
        MultiFn multiFn = ((MultiFn)const__126.getRawRoot()).addMethod(const__127, (IFn)new slf4j$fn__9000());
        MultiFn multiFn2 = ((MultiFn)const__126.getRawRoot()).addMethod(const__128, (IFn)new slf4j$fn__9005());
        MultiFn multiFn3 = ((MultiFn)const__126.getRawRoot()).addMethod((Object)const__129, (IFn)new slf4j$fn__9009());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.slf4j");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.slf4j", (String)"pid");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__8 = RT.var((String)"clojure.edn", (String)"read-string");
        const__9 = RT.var((String)"clojure.string", (String)"replace");
        const__10 = Pattern.compile("@.*");
        const__11 = RT.var((String)"clojure.core", (String)"str");
        const__12 = RT.var((String)"datomic.slf4j", (String)"print-safely");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"o"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.String")})))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.slf4j", (String)"event->timing");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"ddb-values", (String)"put-value-chunk"), RT.keyword(null, (String)"DdbPutChunkMsec"), RT.keyword((String)"io", (String)"gunzip-buffer"), RT.keyword(null, (String)"DecompressMsec"), RT.keyword((String)"peer", (String)"accept-new"), RT.keyword(null, (String)"PeerAcceptNewMsec"), RT.keyword((String)"index", (String)"write-val"), RT.keyword(null, (String)"IndexWriteMsec"), RT.keyword((String)"index", (String)"build-fulltext"), RT.keyword(null, (String)"CreateFulltextIndexMsec"), RT.keyword((String)"io", (String)"gzip-buffer"), RT.keyword(null, (String)"CompressMsec"), RT.keyword((String)"peer", (String)"integrate-lucene"), RT.keyword(null, (String)"PeerIntegrateLuceneMsec"), RT.keyword((String)"log", (String)"add-next"), RT.keyword(null, (String)"LogWriteMsec"), RT.keyword((String)"kv-cluster", (String)"create-val"), RT.keyword(null, (String)"StoragePutMsec"), RT.keyword((String)"clusterfs", (String)"write-val"), RT.keyword(null, (String)"FulltextWriteMsec"), RT.keyword((String)"index", (String)"add-avet"), RT.keyword(null, (String)"AddIndexMsec"), RT.keyword((String)"kv-cluster", (String)"get-pod"), RT.keyword(null, (String)"PodGetMsec"), RT.keyword((String)"kv-cluster", (String)"get-val"), RT.keyword(null, (String)"StorageGetMsec"), RT.keyword((String)"tx", (String)"process"), RT.keyword(null, (String)"TransactionMsec"), RT.keyword((String)"fressian", (String)"defressian"), RT.keyword(null, (String)"DefressianMsec"), RT.keyword((String)"fressian", (String)"fressian"), RT.keyword(null, (String)"FressianMsec"), RT.keyword((String)"kv-cluster", (String)"update-pod"), RT.keyword(null, (String)"PodUpdateMsec"), RT.keyword((String)"db", (String)"accept-index"), RT.keyword(null, (String)"AcceptIndexMsec"), RT.keyword((String)"update", (String)"create-index"), RT.keyword(null, (String)"CreateEntireIndexMsec"), RT.keyword((String)"clusterfs", (String)"create-fs"), RT.keyword(null, (String)"StorageCreateFSMsec"), RT.keyword((String)"db", (String)"add-fulltext"), RT.keyword(null, (String)"DbAddFulltextMsec"), RT.keyword((String)"fressian", (String)"decompress"), RT.keyword(null, (String)"DecompressFressianMsec")});
        const__63 = RT.var((String)"datomic.slf4j", (String)"process");
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"msg"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.String")})))), RT.keyword(null, (String)"column"), 1});
        const__66 = RT.var((String)"datomic.slf4j", (String)"caused-by");
        const__68 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"logger")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Logger")})), (Object)((IObj)Symbol.intern(null, (String)"t")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Throwable")}))))), RT.keyword(null, (String)"column"), 1});
        const__69 = RT.var((String)"datomic.slf4j", (String)"enabled-method");
        const__72 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"level")))), RT.keyword(null, (String)"column"), 1});
        const__73 = RT.var((String)"datomic.slf4j", (String)"log-expr");
        const__75 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"level"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"level"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"ex")))), RT.keyword(null, (String)"column"), 1});
        const__76 = RT.var((String)"datomic.slf4j", (String)"trace");
        const__78 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"ex")))), RT.keyword(null, (String)"column"), 1});
        const__79 = RT.var((String)"datomic.slf4j", (String)"debug");
        const__81 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"ex")))), RT.keyword(null, (String)"column"), 1});
        const__82 = RT.var((String)"datomic.slf4j", (String)"info");
        const__84 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"ex")))), RT.keyword(null, (String)"column"), 1});
        const__85 = RT.var((String)"datomic.slf4j", (String)"warn");
        const__87 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"ex")))), RT.keyword(null, (String)"column"), 1});
        const__88 = RT.var((String)"datomic.slf4j", (String)"error");
        const__90 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"ex")))), RT.keyword(null, (String)"column"), 1});
        const__91 = RT.var((String)"datomic.slf4j", (String)"exception-string");
        const__93 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"t")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Throwable")}))))), RT.keyword(null, (String)"column"), 1});
        const__94 = RT.var((String)"datomic.slf4j", (String)"log-agent-error");
        const__96 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"agent"), (Object)Symbol.intern(null, (String)"error")))), RT.keyword(null, (String)"column"), 1});
        const__97 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
        const__99 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"nsec")))), RT.keyword(null, (String)"column"), 1});
    }

    public static void __init1() {
        const__100 = RT.var((String)"datomic.slf4j", (String)"format-as-musec");
        const__102 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"nsec")))), RT.keyword(null, (String)"column"), 1});
        const__103 = RT.var((String)"datomic.slf4j", (String)"metric-expr");
        const__105 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"event"), (Object)Symbol.intern(null, (String)"msec")))), RT.keyword(null, (String)"column"), 1});
        const__106 = RT.var((String)"datomic.slf4j", (String)"log-end-phase-only-events");
        const__107 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__108 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword((String)"kv-cluster", (String)"create-val")});
        const__109 = RT.var((String)"datomic.slf4j", (String)"log-time");
        const__111 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__112 = RT.var((String)"datomic.slf4j", (String)"dont-log-time");
        const__114 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__115 = RT.var((String)"datomic.slf4j", (String)"sanitize-uri");
        const__117 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"uri")))), RT.keyword(null, (String)"column"), 1});
        const__118 = RT.var((String)"datomic.slf4j", (String)"print-and-warn");
        const__120 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg")))), RT.keyword(null, (String)"column"), 1});
        const__121 = RT.var((String)"clojure.core", (String)"reset!");
        const__122 = RT.var((String)"datomic.error", (String)"reporter");
        const__123 = RT.var((String)"datomic.slf4j", (String)"log-uncaught-exceptions");
        const__125 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__126 = RT.var((String)"datomic.slf4j", (String)"redact");
        const__127 = RT.classForName((String)"java.util.Map");
        const__128 = RT.classForName((String)"java.util.Collection");
        const__129 = RT.keyword(null, (String)"default");
    }

    static {
        slf4j__init.__init0();
        slf4j__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.slf4j__init").getClassLoader());
        try {
            slf4j__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


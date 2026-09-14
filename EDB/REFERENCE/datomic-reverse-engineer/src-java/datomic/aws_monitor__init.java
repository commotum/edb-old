/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AReference
 *  clojure.lang.Compiler
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
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
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.aws_monitor$cloudwatch_reporter;
import datomic.aws_monitor$create_cloudwatch_reporter;
import datomic.aws_monitor$create_request_map;
import datomic.aws_monitor$fn__23567;
import datomic.aws_monitor$fn__23571;
import datomic.aws_monitor$fn__23574;
import datomic.aws_monitor$fn__23585;
import datomic.aws_monitor$fn__23590;
import datomic.aws_monitor$fn__23593;
import datomic.aws_monitor$fn__23606;
import datomic.aws_monitor$fn__23609;
import datomic.aws_monitor$fn__23636;
import datomic.aws_monitor$loading__6434__auto____23565;
import datomic.aws_monitor$partitioned_metrics_requests;
import datomic.aws_monitor$report_metrics;
import datomic.aws_monitor$to_metric_data;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class aws_monitor__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final Keyword const__2;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Object const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final ISeq const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__16;
    public static final Keyword const__17;
    public static final AFn const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final AFn const__22;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final AFn const__27;
    public static final AFn const__28;
    public static final Keyword const__29;
    public static final Var const__30;
    public static final AFn const__31;
    public static final Var const__32;
    public static final Object const__33;
    public static final Var const__34;
    public static final AFn const__37;
    public static final AFn const__63;
    public static final Var const__64;
    public static final Var const__65;
    public static final Var const__66;
    public static final Var const__67;
    public static final Object const__68;
    public static final Var const__69;
    public static final ISeq const__70;
    public static final AFn const__72;
    public static final AFn const__73;
    public static final Keyword const__74;
    public static final AFn const__75;
    public static final AFn const__76;
    public static final AFn const__77;
    public static final AFn const__78;
    public static final Object const__79;
    public static final Object const__80;
    public static final Var const__81;
    public static final AFn const__84;
    public static final Var const__85;
    public static final AFn const__87;
    public static final Object const__88;
    public static final Var const__89;
    public static final AFn const__91;
    public static final Var const__92;
    public static final AFn const__93;
    public static final Object const__94;
    public static final Var const__95;
    public static final AFn const__97;
    public static final Var const__98;
    public static final AFn const__100;
    public static final Var const__101;
    public static final AFn const__103;
    public static final Var const__104;
    public static final AFn const__106;
    public static final Var const__107;
    public static final AFn const__109;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__3);
        Object object2 = ((IFn)new aws_monitor$loading__6434__auto____23565()).invoke();
        if (((Symbol)const__1).equals((Object)const__4)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new aws_monitor$fn__23567())));
            v3 = null;
        }
        Object object3 = ((IFn)new aws_monitor$fn__23571()).invoke();
        Object object4 = const__5;
        Object object5 = ((IFn)const__6.getRawRoot()).invoke((Object)const__7, const__8.getRawRoot(), (Object)const__2, null);
        Object object6 = ((IFn)const__9).invoke((Object)const__7, (Object)const__10);
        Object object7 = ((IFn)const__11.getRawRoot()).invoke((Object)const__7, const__12.getRawRoot(), ((IFn)const__8.getRawRoot()).invoke((Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__7, (Object)const__20, (Object)const__22, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__27, ((IFn)const__12.getRawRoot()).invoke((Object)const__28, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__7})))), new aws_monitor$fn__23574()})));
        Object object8 = ((IFn)const__30.getRawRoot()).invoke(const__7.getRawRoot());
        AFn aFn = const__31;
        Object object9 = ((IFn)const__32.getRawRoot()).invoke(const__33, const__7.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new aws_monitor$fn__23585()}));
        Var var = const__34;
        var.setMeta((IPersistentMap)const__37);
        Var var2 = var;
        var.bindRoot(((IFn)const__12.getRawRoot()).invoke((Object)const__63, ((IFn)const__64.getRawRoot()).invoke(((IFn)const__65.getRawRoot()).invoke(const__66.getRawRoot()), ((IFn)const__67.getRawRoot()).invoke((Object)"Milliseconds"))));
        Object object10 = ((IFn)new aws_monitor$fn__23590()).invoke();
        Object object11 = const__68;
        Object object12 = ((IFn)const__6.getRawRoot()).invoke((Object)const__69, const__8.getRawRoot(), (Object)const__2, null);
        Object object13 = ((IFn)const__9).invoke((Object)const__69, (Object)const__70);
        Object object14 = ((IFn)const__11.getRawRoot()).invoke((Object)const__69, const__12.getRawRoot(), ((IFn)const__8.getRawRoot()).invoke((Object)const__72, (Object)const__17, (Object)const__73, (Object)const__19, (Object)const__69, (Object)const__20, (Object)const__75, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__76, ((IFn)const__12.getRawRoot()).invoke((Object)const__77, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__69})))), new aws_monitor$fn__23593()})));
        Object object15 = ((IFn)const__30.getRawRoot()).invoke(const__69.getRawRoot());
        AFn aFn2 = const__78;
        Object object16 = ((IFn)const__32.getRawRoot()).invoke(const__79, const__69.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__74, new aws_monitor$fn__23606()}));
        Object object17 = ((IFn)const__32.getRawRoot()).invoke(const__80, const__69.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__74, new aws_monitor$fn__23609()}));
        Var var3 = const__81;
        var3.setMeta((IPersistentMap)const__84);
        Var var4 = var3;
        var3.bindRoot((Object)new aws_monitor$to_metric_data());
        Var var5 = const__85;
        var5.setMeta((IPersistentMap)const__87);
        Var var6 = var5;
        var5.bindRoot(const__88);
        Var var7 = const__89;
        var7.setMeta((IPersistentMap)const__91);
        Var var8 = var7;
        var7.bindRoot((Object)new aws_monitor$create_request_map());
        Var var9 = const__92;
        var9.setMeta((IPersistentMap)const__93);
        Var var10 = var9;
        var9.bindRoot(const__94);
        Var var11 = const__95;
        var11.setMeta((IPersistentMap)const__97);
        Var var12 = var11;
        var11.bindRoot((Object)new aws_monitor$partitioned_metrics_requests());
        Var var13 = const__98;
        var13.setMeta((IPersistentMap)const__100);
        Var var14 = var13;
        var13.bindRoot((Object)new aws_monitor$report_metrics());
        Var var15 = const__101;
        var15.setMeta((IPersistentMap)const__103);
        Var var16 = var15;
        var15.bindRoot((Object)new aws_monitor$create_cloudwatch_reporter());
        Var var17 = const__104;
        var17.setMeta((IPersistentMap)const__106);
        Var var18 = var17;
        var17.bindRoot((Object)new Delay((IFn)new aws_monitor$fn__23636()));
        Var var19 = const__107;
        var19.setMeta((IPersistentMap)const__109);
        Var var20 = var19;
        var19.bindRoot((Object)new aws_monitor$cloudwatch_reporter());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.aws-monitor")).withMeta(RT.map((Object[])new Object[0]));
        const__2 = RT.keyword(null, (String)"doc");
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "Functions to publish monitoring data to AWS."});
        const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__5 = RT.classForName((String)"datomic.aws_monitor.Qn");
        const__6 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__7 = RT.var((String)"datomic.aws-monitor", (String)"Qn");
        const__8 = RT.var((String)"clojure.core", (String)"assoc");
        const__9 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__10 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"qualified-name")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__11 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.aws_monitor.Qn"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.aws_monitor.Qn")});
        const__17 = RT.keyword(null, (String)"sigs");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"qualified-name"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"qualified-name")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), null})});
        const__19 = RT.keyword(null, (String)"var");
        const__20 = RT.keyword(null, (String)"method-map");
        const__21 = RT.keyword(null, (String)"qualified-name");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"qualified-name"), RT.keyword(null, (String)"qualified-name")});
        const__23 = RT.keyword(null, (String)"method-builders");
        const__24 = RT.var((String)"clojure.core", (String)"intern");
        const__25 = RT.var((String)"clojure.core", (String)"*ns*");
        const__26 = RT.var((String)"clojure.core", (String)"with-meta");
        const__27 = (AFn)((IObj)Symbol.intern(null, (String)"qualified-name")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"qualified-name")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), null});
        const__29 = RT.keyword(null, (String)"protocol");
        const__30 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__31 = (AFn)Symbol.intern(null, (String)"Qn");
        const__32 = RT.var((String)"clojure.core", (String)"extend");
        const__33 = RT.classForName((String)"clojure.lang.Keyword");
        const__34 = RT.var((String)"datomic.aws-monitor", (String)"units");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"WriterMemcachedPutFailedMusec"), "Microseconds", RT.keyword(null, (String)"MemoryIndexMB"), "Megabytes", RT.keyword(null, (String)"ReaderMemcachedPutFailedMusec"), "Microseconds", RT.keyword(null, (String)"HeartMonitorMsec"), "Milliseconds", RT.keyword(null, (String)"ValcacheGetFailedMsec"), "Milliseconds", RT.keyword(null, (String)"ValcachePutSucceededMsec"), "Milliseconds", RT.keyword(null, (String)"MemcachedGetFailedMsec"), "Milliseconds", RT.keyword(null, (String)"MemcachedPutSucceededMsec"), "Milliseconds", RT.keyword(null, (String)"TransactionBytes"), "Bytes", RT.keyword(null, (String)"AvailableMB"), "Megabytes", RT.keyword(null, (String)"ValueSize"), "Bytes", RT.keyword(null, (String)"ReaderMemcachedPutMusec"), "Microseconds", RT.keyword(null, (String)"HeartbeatMsec"), "Milliseconds", RT.keyword(null, (String)"MemcachedGetSucceededMsec"), "Milliseconds", RT.keyword(null, (String)"WriterMemcachedPutMusec"), "Microseconds", RT.keyword(null, (String)"StoragePutBytes"), "Bytes", RT.keyword(null, (String)"LogIngestMsec"), "Milliseconds", RT.keyword(null, (String)"ValcachePutFailedMsec"), "Milliseconds", RT.keyword(null, (String)"ValcacheGetSucceededMsec"), "Milliseconds", RT.keyword(null, (String)"MemoryIndexFillMsec"), "Milliseconds", RT.keyword(null, (String)"StoragePutBackoffMsec"), "Milliseconds", RT.keyword(null, (String)"LogIngestBytes"), "Bytes", RT.keyword(null, (String)"MemcachedPutFailedMsec"), "Milliseconds", RT.keyword(null, (String)"StorageGetBytes"), "Bytes", RT.keyword(null, (String)"StorageGetBackoffMsec"), "Milliseconds"});
        const__64 = RT.var((String)"clojure.core", (String)"zipmap");
        const__65 = RT.var((String)"clojure.core", (String)"vals");
        const__66 = RT.var((String)"datomic.slf4j", (String)"event->timing");
        const__67 = RT.var((String)"clojure.core", (String)"repeat");
        const__68 = RT.classForName((String)"datomic.aws_monitor.ToMetricData");
        const__69 = RT.var((String)"datomic.aws-monitor", (String)"ToMetricData");
        const__70 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"to-metric-data-helper")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"k"))))}))));
        const__72 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.aws_monitor.ToMetricData"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.aws_monitor.ToMetricData")});
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"to-metric-data-helper"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"to-metric-data-helper")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), "argument flipping helper for to-metric-data"})});
        const__74 = RT.keyword(null, (String)"to-metric-data-helper");
        const__75 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"to-metric-data-helper"), RT.keyword(null, (String)"to-metric-data-helper")});
        const__76 = (AFn)((IObj)Symbol.intern(null, (String)"to-metric-data-helper")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"k"))))}));
        const__77 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"to-metric-data-helper")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), "argument flipping helper for to-metric-data"});
        const__78 = (AFn)Symbol.intern(null, (String)"ToMetricData");
        const__79 = RT.classForName((String)"java.util.Map");
        const__80 = RT.classForName((String)"java.lang.Number");
        const__81 = RT.var((String)"datomic.aws-monitor", (String)"to-metric-data");
        const__84 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"statistics")))), RT.keyword(null, (String)"column"), 1});
        const__85 = RT.var((String)"datomic.aws-monitor", (String)"AWS_METRIC_DATA_COUNT_LIMIT");
        const__87 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__88 = 20L;
        const__89 = RT.var((String)"datomic.aws-monitor", (String)"create-request-map");
        const__91 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"dimensions"), (Object)Symbol.intern(null, (String)"metrics")))), RT.keyword(null, (String)"column"), 1});
        const__92 = RT.var((String)"datomic.aws-monitor", (String)"MILLION");
        const__93 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__94 = 1000000L;
        const__95 = RT.var((String)"datomic.aws-monitor", (String)"partitioned-metrics-requests");
        const__97 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"dimensions"), (Object)Symbol.intern(null, (String)"metrics")))), RT.keyword(null, (String)"column"), 1});
        const__98 = RT.var((String)"datomic.aws-monitor", (String)"report-metrics");
    }

    public static void __init1() {
        const__100 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"dimensions"), (Object)Symbol.intern(null, (String)"metrics")))), RT.keyword(null, (String)"column"), 1});
        const__101 = RT.var((String)"datomic.aws-monitor", (String)"create-cloudwatch-reporter");
        const__103 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"name"), (Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"aws-cloudwatch-dimension-value"), (Object)Symbol.intern(null, (String)"aws-cloudwatch-region"))})))), RT.keyword(null, (String)"column"), 1});
        const__104 = RT.var((String)"datomic.aws-monitor", (String)"cloudwatch-reporter-ref");
        const__106 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__107 = RT.var((String)"datomic.aws-monitor", (String)"cloudwatch-reporter");
        const__109 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        aws_monitor__init.__init0();
        aws_monitor__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.aws_monitor__init").getClassLoader());
        try {
            aws_monitor__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


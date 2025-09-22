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
import datomic.cloudwatch$client;
import datomic.cloudwatch$describe_alarms;
import datomic.cloudwatch$fn__22809;
import datomic.cloudwatch$fn__22817;
import datomic.cloudwatch$fn__22821;
import datomic.cloudwatch$fn__22825;
import datomic.cloudwatch$fn__22829;
import datomic.cloudwatch$fn__22833;
import datomic.cloudwatch$fn__22837;
import datomic.cloudwatch$fn__22843;
import datomic.cloudwatch$fn__22849;
import datomic.cloudwatch$fn__22911;
import datomic.cloudwatch$fn__22923;
import datomic.cloudwatch$fn__22926;
import datomic.cloudwatch$fn__22929;
import datomic.cloudwatch$fn__22932;
import datomic.cloudwatch$fn__22935;
import datomic.cloudwatch$fn__22938;
import datomic.cloudwatch$fn__22941;
import datomic.cloudwatch$fn__22944;
import datomic.cloudwatch$fn__22947;
import datomic.cloudwatch$fn__22949;
import datomic.cloudwatch$get_metric_statistics;
import datomic.cloudwatch$list_metrics;
import datomic.cloudwatch$loading__6434__auto____22807;
import datomic.cloudwatch$put_metrics;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class cloudwatch__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final AFn const__18;
    public static final Object const__19;
    public static final Var const__20;
    public static final AFn const__21;
    public static final Object const__22;
    public static final AFn const__23;
    public static final AFn const__24;
    public static final AFn const__26;
    public static final Object const__27;
    public static final AFn const__28;
    public static final AFn const__31;
    public static final AFn const__32;
    public static final AFn const__35;
    public static final Object const__36;
    public static final AFn const__37;
    public static final Var const__38;
    public static final Var const__39;
    public static final Keyword const__40;
    public static final Object const__41;
    public static final Object const__42;
    public static final Var const__43;
    public static final AFn const__45;
    public static final AFn const__46;
    public static final AFn const__47;
    public static final AFn const__48;
    public static final AFn const__49;
    public static final AFn const__50;
    public static final AFn const__52;
    public static final AFn const__53;
    public static final AFn const__56;
    public static final AFn const__58;
    public static final Var const__59;
    public static final AFn const__63;
    public static final Var const__64;
    public static final AFn const__66;
    public static final Var const__67;
    public static final AFn const__69;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new cloudwatch$loading__6434__auto____22807()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new cloudwatch$fn__22809())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new cloudwatch$client());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new cloudwatch$list_metrics());
        Object object4 = ((IFn)const__13.getRawRoot()).invoke((Object)const__14, const__15.getRawRoot(), (Object)const__18, const__19);
        MultiFn multiFn = ((MultiFn)const__20.getRawRoot()).addMethod((Object)const__21, (IFn)new cloudwatch$fn__22817());
        Object object5 = ((IFn)const__13.getRawRoot()).invoke((Object)const__14, const__15.getRawRoot(), (Object)const__23, const__19);
        MultiFn multiFn2 = ((MultiFn)const__20.getRawRoot()).addMethod((Object)const__24, (IFn)new cloudwatch$fn__22821());
        Object object6 = ((IFn)const__13.getRawRoot()).invoke((Object)const__14, const__15.getRawRoot(), (Object)const__26, const__27);
        MultiFn multiFn3 = ((MultiFn)const__20.getRawRoot()).addMethod((Object)const__28, (IFn)new cloudwatch$fn__22825());
        Object object7 = ((IFn)const__13.getRawRoot()).invoke((Object)const__14, const__15.getRawRoot(), (Object)const__31, const__22);
        MultiFn multiFn4 = ((MultiFn)const__20.getRawRoot()).addMethod((Object)const__32, (IFn)new cloudwatch$fn__22829());
        Object object8 = ((IFn)const__13.getRawRoot()).invoke((Object)const__14, const__15.getRawRoot(), (Object)const__35, const__36);
        MultiFn multiFn5 = ((MultiFn)const__20.getRawRoot()).addMethod((Object)const__37, (IFn)new cloudwatch$fn__22833());
        Object object9 = ((IFn)const__38.getRawRoot()).invoke(const__19, const__39.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__40, new cloudwatch$fn__22837()}));
        Object object10 = ((IFn)const__38.getRawRoot()).invoke(const__27, const__39.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__40, new cloudwatch$fn__22843()}));
        Object object11 = ((IFn)const__38.getRawRoot()).invoke(const__41, const__39.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__40, new cloudwatch$fn__22849()}));
        Object object12 = ((IFn)const__38.getRawRoot()).invoke(const__42, const__39.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__40, new cloudwatch$fn__22911()}));
        MultiFn multiFn6 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__45, (IFn)new cloudwatch$fn__22923());
        MultiFn multiFn7 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__46, (IFn)new cloudwatch$fn__22926());
        MultiFn multiFn8 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__47, (IFn)new cloudwatch$fn__22929());
        MultiFn multiFn9 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__48, (IFn)new cloudwatch$fn__22932());
        MultiFn multiFn10 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__49, (IFn)new cloudwatch$fn__22935());
        MultiFn multiFn11 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__50, (IFn)new cloudwatch$fn__22938());
        MultiFn multiFn12 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__52, (IFn)new cloudwatch$fn__22941());
        MultiFn multiFn13 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__53, (IFn)new cloudwatch$fn__22944());
        MultiFn multiFn14 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__56, (IFn)new cloudwatch$fn__22947());
        MultiFn multiFn15 = ((MultiFn)const__43.getRawRoot()).addMethod((Object)const__58, (IFn)new cloudwatch$fn__22949());
        Var var5 = const__59;
        var5.setMeta((IPersistentMap)const__63);
        Var var6 = var5;
        var5.bindRoot((Object)new cloudwatch$get_metric_statistics());
        Var var7 = const__64;
        var7.setMeta((IPersistentMap)const__66);
        Var var8 = var7;
        var7.bindRoot((Object)new cloudwatch$put_metrics());
        Var var9 = const__67;
        var9.setMeta((IPersistentMap)const__69);
        Var var10 = var9;
        var9.bindRoot((Object)new cloudwatch$describe_alarms());
        Object v38 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cloudwatch");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.cloudwatch", (String)"client");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create()).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.cloudwatch.AmazonCloudWatchClient")})), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"creds"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.cloudwatch.AmazonCloudWatchClient")})), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"config"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.cloudwatch.AmazonCloudWatchClient")})))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.cloudwatch", (String)"list-metrics");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"client")), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"client")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"AmazonCloudWatchClient")})), (Object)Symbol.intern(null, (String)"request")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__14 = RT.var((String)"datomic.datafy", (String)"list-property-types");
        const__15 = RT.var((String)"clojure.core", (String)"assoc");
        const__18 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest"), (Object)RT.keyword(null, (String)"dimensions"));
        const__19 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.Dimension");
        const__20 = RT.var((String)"datomic.datafy", (String)"property-to-object");
        const__21 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest"), (Object)RT.keyword(null, (String)"dimensions"));
        const__22 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.MetricDatum");
        const__23 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.MetricDatum"), (Object)RT.keyword(null, (String)"dimensions"));
        const__24 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.MetricDatum"), (Object)RT.keyword(null, (String)"dimensions"));
        const__26 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.ListMetricsRequest"), (Object)RT.keyword(null, (String)"dimensions"));
        const__27 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.DimensionFilter");
        const__28 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.ListMetricsRequest"), (Object)RT.keyword(null, (String)"dimensions"));
        const__31 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.PutMetricDataRequest"), (Object)RT.keyword(null, (String)"metricData"));
        const__32 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.PutMetricDataRequest"), (Object)RT.keyword(null, (String)"metricData"));
        const__35 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest"), (Object)RT.keyword(null, (String)"alarmNames"));
        const__36 = RT.classForName((String)"java.lang.String");
        const__37 = (AFn)Tuple.create((Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest"), (Object)RT.keyword(null, (String)"alarmNames"));
        const__38 = RT.var((String)"clojure.core", (String)"extend");
        const__39 = RT.var((String)"datomic.datafy", (String)"ObjectToData");
        const__40 = RT.keyword(null, (String)"object-to-data");
        const__41 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.MetricAlarm");
        const__42 = RT.classForName((String)"com.amazonaws.services.cloudwatch.model.DescribeAlarmsResult");
        const__43 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__45 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.Dimension"));
        const__46 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.DimensionFilter"));
        const__47 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest"));
        const__48 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.ListMetricsRequest"));
        const__49 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.PutMetricDataRequest"));
        const__50 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.MetricDatum"));
        const__52 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.StatisticSet"));
        const__53 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.DescribeAlarmsRequest"));
        const__56 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"atom"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.StateValue"));
        const__58 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"atom"), (Object)RT.classForName((String)"com.amazonaws.services.cloudwatch.model.StandardUnit"));
        const__59 = RT.var((String)"datomic.cloudwatch", (String)"get-metric-statistics");
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.cloudwatch.AmazonCloudWatchClient"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.cloudwatch.AmazonCloudWatchClient")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__64 = RT.var((String)"datomic.cloudwatch", (String)"put-metrics");
        const__66 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.cloudwatch.AmazonCloudWatchClient"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.cloudwatch.AmazonCloudWatchClient")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
        const__67 = RT.var((String)"datomic.cloudwatch", (String)"describe-alarms");
        const__69 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"related-class"), RT.classForName((String)"com.amazonaws.services.cloudwatch.AmazonCloudWatchClient"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"o")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.services.cloudwatch.AmazonCloudWatchClient")})), (Object)Symbol.intern(null, (String)"x1")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        cloudwatch__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cloudwatch__init").getClassLoader());
        try {
            cloudwatch__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


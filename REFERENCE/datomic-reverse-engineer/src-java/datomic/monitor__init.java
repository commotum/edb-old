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
import datomic.monitor$add_stat;
import datomic.monitor$alarm;
import datomic.monitor$fn__496;
import datomic.monitor$fn__500;
import datomic.monitor$fn__503;
import datomic.monitor$fn__514;
import datomic.monitor$fn__516;
import datomic.monitor$fn__522;
import datomic.monitor$init_stats;
import datomic.monitor$load_callback;
import datomic.monitor$loading__6434__auto____475;
import datomic.monitor$ns__GT_ms;
import datomic.monitor$reify__518;
import datomic.monitor$reify__520;
import datomic.monitor$snapshot_statistics;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class monitor__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final Keyword const__2;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Var const__5;
    public static final Object const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final ISeq const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final AFn const__17;
    public static final Keyword const__18;
    public static final AFn const__19;
    public static final Keyword const__20;
    public static final Keyword const__21;
    public static final Keyword const__22;
    public static final AFn const__23;
    public static final Keyword const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final Var const__27;
    public static final AFn const__28;
    public static final AFn const__29;
    public static final Keyword const__30;
    public static final Var const__31;
    public static final AFn const__32;
    public static final Var const__33;
    public static final Object const__34;
    public static final Var const__35;
    public static final AFn const__38;
    public static final AFn const__42;
    public static final Var const__43;
    public static final AFn const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__51;
    public static final Var const__52;
    public static final AFn const__53;
    public static final Var const__54;
    public static final Var const__55;
    public static final AFn const__57;
    public static final Var const__58;
    public static final AFn const__60;
    public static final Var const__61;
    public static final AFn const__62;
    public static final Var const__63;
    public static final AFn const__65;
    public static final Var const__66;
    public static final AFn const__68;
    public static final Var const__69;
    public static final AFn const__71;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__3);
        Object object2 = ((IFn)new monitor$loading__6434__auto____475()).invoke();
        if (((Symbol)const__1).equals((Object)const__4)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new monitor$fn__496())));
            v3 = null;
        }
        Object object3 = const__5.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new monitor$fn__500()).invoke();
        Object object5 = const__6;
        Object object6 = ((IFn)const__7.getRawRoot()).invoke((Object)const__8, const__9.getRawRoot(), (Object)const__2, null);
        Object object7 = ((IFn)const__10).invoke((Object)const__8, (Object)const__11);
        Object object8 = ((IFn)const__12.getRawRoot()).invoke((Object)const__8, const__13.getRawRoot(), ((IFn)const__9.getRawRoot()).invoke((Object)const__17, (Object)const__18, (Object)const__19, (Object)const__20, (Object)const__8, (Object)const__21, (Object)const__23, (Object)const__24, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__25.getRawRoot()).invoke(const__26.get(), ((IFn)const__27.getRawRoot()).invoke((Object)const__28, ((IFn)const__13.getRawRoot()).invoke((Object)const__29, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__30, const__8})))), new monitor$fn__503()})));
        Object object9 = ((IFn)const__31.getRawRoot()).invoke(const__8.getRawRoot());
        AFn aFn = const__32;
        Object object10 = ((IFn)const__33.getRawRoot()).invoke(const__34, const__8.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__22, new monitor$fn__514()}));
        Object object11 = ((IFn)new monitor$fn__516()).invoke();
        Var var = const__35;
        var.setMeta((IPersistentMap)const__38);
        Var var2 = var;
        var.bindRoot((Object)((IObj)new monitor$reify__518(null)).withMeta((IPersistentMap)const__42));
        Var var3 = const__43;
        var3.setMeta((IPersistentMap)const__44);
        Var var4 = var3;
        var3.bindRoot((Object)((IObj)new monitor$reify__520(null)).withMeta((IPersistentMap)const__46));
        Object object12 = ((IFn)new monitor$fn__522()).invoke();
        Var var5 = const__47;
        var5.setMeta((IPersistentMap)const__51);
        Var var6 = var5;
        var5.bindRoot((Object)new monitor$init_stats());
        Var var7 = const__52;
        var7.setMeta((IPersistentMap)const__53);
        Var var8 = var7;
        var7.bindRoot(((IFn)const__54.getRawRoot()).invoke(((IFn)const__47.getRawRoot()).invoke()));
        Var var9 = const__55;
        var9.setMeta((IPersistentMap)const__57);
        Var var10 = var9;
        var9.bindRoot((Object)new monitor$snapshot_statistics());
        Var var11 = const__58;
        var11.setMeta((IPersistentMap)const__60);
        Var var12 = var11;
        var11.bindRoot((Object)new monitor$load_callback());
        Var var13 = const__61;
        var13.setMeta((IPersistentMap)const__62);
        Var var14 = var13;
        var13.bindRoot(((IFn)const__58.getRawRoot()).invoke((Object)"datomic.metricEventCallback"));
        Var var15 = const__63;
        var15.setMeta((IPersistentMap)const__65);
        Var var16 = var15;
        var15.bindRoot((Object)new monitor$add_stat());
        Var var17 = const__66;
        var17.setMeta((IPersistentMap)const__68);
        Var var18 = var17;
        var17.bindRoot((Object)new monitor$ns__GT_ms());
        Var var19 = const__69;
        var19.setMeta((IPersistentMap)const__71);
        Var var20 = var19;
        var19.bindRoot((Object)new monitor$alarm());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.monitor")).withMeta(RT.map((Object[])new Object[0]));
        const__2 = RT.keyword(null, (String)"doc");
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "Deployment-agnostic status functions, requiring only a JVM."});
        const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__5 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__6 = RT.classForName((String)"datomic.monitor.Metrics");
        const__7 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__8 = RT.var((String)"datomic.monitor", (String)"Metrics");
        const__9 = RT.var((String)"clojure.core", (String)"assoc");
        const__10 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__11 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"metrics")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__12 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__13 = RT.var((String)"clojure.core", (String)"merge");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.monitor.Metrics"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.monitor.Metrics")});
        const__18 = RT.keyword(null, (String)"sigs");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"metrics"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"metrics")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Return a map of metrics information about an object."})});
        const__20 = RT.keyword(null, (String)"var");
        const__21 = RT.keyword(null, (String)"method-map");
        const__22 = RT.keyword(null, (String)"metrics");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"metrics"), RT.keyword(null, (String)"metrics")});
        const__24 = RT.keyword(null, (String)"method-builders");
        const__25 = RT.var((String)"clojure.core", (String)"intern");
        const__26 = RT.var((String)"clojure.core", (String)"*ns*");
        const__27 = RT.var((String)"clojure.core", (String)"with-meta");
        const__28 = (AFn)((IObj)Symbol.intern(null, (String)"metrics")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"metrics")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Return a map of metrics information about an object."});
        const__30 = RT.keyword(null, (String)"protocol");
        const__31 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__32 = (AFn)Symbol.intern(null, (String)"Metrics");
        const__33 = RT.var((String)"clojure.core", (String)"extend");
        const__34 = RT.classForName((String)"java.lang.Runtime");
        const__35 = RT.var((String)"datomic.monitor", (String)"min*");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 32, RT.keyword(null, (String)"column"), 3});
        const__43 = RT.var((String)"datomic.monitor", (String)"max*");
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 36, RT.keyword(null, (String)"column"), 3});
        const__47 = RT.var((String)"datomic.monitor", (String)"init-stats");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.monitor", (String)"statistics");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"clojure.core", (String)"atom");
        const__55 = RT.var((String)"datomic.monitor", (String)"snapshot-statistics");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.monitor", (String)"load-callback");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"prop-name")))), RT.keyword(null, (String)"column"), 1});
        const__61 = RT.var((String)"datomic.monitor", (String)"metric-event-callback");
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__63 = RT.var((String)"datomic.monitor", (String)"add-stat");
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"val")))), RT.keyword(null, (String)"column"), 1});
        const__66 = RT.var((String)"datomic.monitor", (String)"ns->ms");
        const__68 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)((IObj)Symbol.intern(null, (String)"nanos")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"double")})))), RT.keyword(null, (String)"column"), 1});
        const__69 = RT.var((String)"datomic.monitor", (String)"alarm");
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        monitor__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.monitor__init").getClassLoader());
        try {
            monitor__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


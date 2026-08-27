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
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.connector$admin_request;
import datomic.connector$create_hornet_factory;
import datomic.connector$create_hornet_notifier;
import datomic.connector$create_transactor_hornet_connector;
import datomic.connector$endpoint_error;
import datomic.connector$fn__20972;
import datomic.connector$fn__20976;
import datomic.connector$fn__20979;
import datomic.connector$fn__21000;
import datomic.connector$fn__21003;
import datomic.connector$fn__21016;
import datomic.connector$fn__21031;
import datomic.connector$fn__21042;
import datomic.connector$fn__21055;
import datomic.connector$fn__21076;
import datomic.connector$fn__21079;
import datomic.connector$fn__21096;
import datomic.connector$fn__21111;
import datomic.connector$fn__21122;
import datomic.connector$fn__21170;
import datomic.connector$fn__21177;
import datomic.connector$fn__21183;
import datomic.connector$fn__21185;
import datomic.connector$fn__21187;
import datomic.connector$fn__21189;
import datomic.connector$fn__21226;
import datomic.connector$host_order;
import datomic.connector$loading__6434__auto____20766;
import datomic.connector$stop_all_connectors;
import datomic.connector$try_hornet_connect;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class connector__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Object const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final ISeq const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__16;
    public static final Keyword const__17;
    public static final AFn const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
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
    public static final Object const__32;
    public static final Var const__33;
    public static final ISeq const__34;
    public static final AFn const__36;
    public static final AFn const__37;
    public static final AFn const__43;
    public static final AFn const__44;
    public static final AFn const__45;
    public static final AFn const__46;
    public static final AFn const__47;
    public static final AFn const__48;
    public static final AFn const__49;
    public static final AFn const__50;
    public static final AFn const__51;
    public static final AFn const__52;
    public static final AFn const__53;
    public static final AFn const__54;
    public static final Object const__55;
    public static final Var const__56;
    public static final ISeq const__57;
    public static final AFn const__59;
    public static final AFn const__60;
    public static final AFn const__65;
    public static final AFn const__66;
    public static final AFn const__67;
    public static final AFn const__68;
    public static final AFn const__69;
    public static final AFn const__70;
    public static final AFn const__71;
    public static final AFn const__72;
    public static final AFn const__73;
    public static final AFn const__74;
    public static final Var const__75;
    public static final AFn const__80;
    public static final Var const__81;
    public static final AFn const__83;
    public static final Var const__84;
    public static final AFn const__85;
    public static final Var const__86;
    public static final Object const__87;
    public static final Var const__88;
    public static final AFn const__90;
    public static final Var const__91;
    public static final AFn const__94;
    public static final Var const__95;
    public static final AFn const__97;
    public static final Var const__98;
    public static final AFn const__100;
    public static final Var const__101;
    public static final Keyword const__102;
    public static final Keyword const__103;
    public static final Keyword const__104;
    public static final Keyword const__105;
    public static final Var const__106;
    public static final AFn const__108;
    public static final Var const__109;
    public static final AFn const__111;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new connector$loading__6434__auto____20766()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new connector$fn__20972())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new connector$fn__20976()).invoke();
        Object object5 = const__4;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, const__7.getRawRoot(), (Object)const__8, null);
        Object object7 = ((IFn)const__9).invoke((Object)const__6, (Object)const__10);
        Object object8 = ((IFn)const__11.getRawRoot()).invoke((Object)const__6, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__6, (Object)const__20, (Object)const__22, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__27, ((IFn)const__12.getRawRoot()).invoke((Object)const__28, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__6})))), new connector$fn__20979()})));
        Object object9 = ((IFn)const__30.getRawRoot()).invoke(const__6.getRawRoot());
        AFn aFn = const__31;
        Object object10 = ((IFn)new connector$fn__21000()).invoke();
        Object object11 = const__32;
        Object object12 = ((IFn)const__5.getRawRoot()).invoke((Object)const__33, const__7.getRawRoot(), (Object)const__8, null);
        Object object13 = ((IFn)const__9).invoke((Object)const__33, (Object)const__34);
        Object object14 = ((IFn)const__11.getRawRoot()).invoke((Object)const__33, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__36, (Object)const__17, (Object)const__37, (Object)const__19, (Object)const__33, (Object)const__20, (Object)const__43, (Object)const__23, (Object)RT.map((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__44, ((IFn)const__12.getRawRoot()).invoke((Object)const__45, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__33})))), new connector$fn__21003(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__46, ((IFn)const__12.getRawRoot()).invoke((Object)const__47, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__33})))), new connector$fn__21016(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__48, ((IFn)const__12.getRawRoot()).invoke((Object)const__49, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__33})))), new connector$fn__21031(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__50, ((IFn)const__12.getRawRoot()).invoke((Object)const__51, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__33})))), new connector$fn__21042(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__52, ((IFn)const__12.getRawRoot()).invoke((Object)const__53, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__33})))), new connector$fn__21055()})));
        Object object15 = ((IFn)const__30.getRawRoot()).invoke(const__33.getRawRoot());
        AFn aFn2 = const__54;
        Object object16 = ((IFn)new connector$fn__21076()).invoke();
        Object object17 = const__55;
        Object object18 = ((IFn)const__5.getRawRoot()).invoke((Object)const__56, const__7.getRawRoot(), (Object)const__8, null);
        Object object19 = ((IFn)const__9).invoke((Object)const__56, (Object)const__57);
        Object object20 = ((IFn)const__11.getRawRoot()).invoke((Object)const__56, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__59, (Object)const__17, (Object)const__60, (Object)const__19, (Object)const__56, (Object)const__20, (Object)const__65, (Object)const__23, (Object)RT.map((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__66, ((IFn)const__12.getRawRoot()).invoke((Object)const__67, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__56})))), new connector$fn__21079(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__68, ((IFn)const__12.getRawRoot()).invoke((Object)const__69, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__56})))), new connector$fn__21096(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__70, ((IFn)const__12.getRawRoot()).invoke((Object)const__71, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__56})))), new connector$fn__21111(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__72, ((IFn)const__12.getRawRoot()).invoke((Object)const__73, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__56})))), new connector$fn__21122()})));
        Object object21 = ((IFn)const__30.getRawRoot()).invoke(const__56.getRawRoot());
        AFn aFn3 = const__74;
        Var var = const__75;
        var.setMeta((IPersistentMap)const__80);
        Var var2 = var;
        var.bindRoot((Object)new connector$admin_request());
        Var var3 = const__81;
        var3.setMeta((IPersistentMap)const__83);
        Var var4 = var3;
        var3.bindRoot((Object)new connector$endpoint_error());
        Var var5 = const__84;
        var5.setMeta((IPersistentMap)const__85);
        Var var6 = var5;
        var5.bindRoot(((IFn)const__86.getRawRoot()).invoke(const__87));
        Var var7 = const__88;
        var7.setMeta((IPersistentMap)const__90);
        Var var8 = var7;
        var7.bindRoot((Object)new connector$stop_all_connectors());
        Var var9 = const__91;
        var9.setMeta((IPersistentMap)const__94);
        Var var10 = var9;
        var9.bindRoot((Object)new connector$try_hornet_connect());
        Var var11 = const__95;
        var11.setMeta((IPersistentMap)const__97);
        Var var12 = var11;
        var11.bindRoot((Object)new connector$host_order());
        Var var13 = const__98;
        var13.setMeta((IPersistentMap)const__100);
        Var var14 = var13;
        var13.bindRoot((Object)new connector$create_hornet_factory());
        Object object22 = ((IFn)new connector$fn__21170()).invoke();
        Object object23 = ((IFn)new connector$fn__21177()).invoke();
        MultiFn multiFn = ((MultiFn)const__101.getRawRoot()).addMethod((Object)const__102, (IFn)new connector$fn__21183());
        MultiFn multiFn2 = ((MultiFn)const__101.getRawRoot()).addMethod((Object)const__103, (IFn)new connector$fn__21185());
        MultiFn multiFn3 = ((MultiFn)const__101.getRawRoot()).addMethod((Object)const__104, (IFn)new connector$fn__21187());
        MultiFn multiFn4 = ((MultiFn)const__101.getRawRoot()).addMethod((Object)const__105, (IFn)new connector$fn__21189());
        Var var15 = const__106;
        var15.setMeta((IPersistentMap)const__108);
        Var var16 = var15;
        var15.bindRoot((Object)new connector$create_hornet_notifier());
        Object object24 = ((IFn)new connector$fn__21226()).invoke();
        Var var17 = const__109;
        var17.setMeta((IPersistentMap)const__111);
        Var var18 = var17;
        var17.bindRoot((Object)new connector$create_transactor_hornet_connector());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.connector");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.classForName((String)"datomic.connector.Startable");
        const__5 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__6 = RT.var((String)"datomic.connector", (String)"Startable");
        const__7 = RT.var((String)"clojure.core", (String)"assoc");
        const__8 = RT.keyword(null, (String)"doc");
        const__9 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__10 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"start")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__11 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.connector.Startable"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.connector.Startable")});
        const__17 = RT.keyword(null, (String)"sigs");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"start"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"start")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Idempotently start a task, returning a future."})});
        const__19 = RT.keyword(null, (String)"var");
        const__20 = RT.keyword(null, (String)"method-map");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"start"), RT.keyword(null, (String)"start")});
        const__23 = RT.keyword(null, (String)"method-builders");
        const__24 = RT.var((String)"clojure.core", (String)"intern");
        const__25 = RT.var((String)"clojure.core", (String)"*ns*");
        const__26 = RT.var((String)"clojure.core", (String)"with-meta");
        const__27 = (AFn)((IObj)Symbol.intern(null, (String)"start")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"start")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Idempotently start a task, returning a future."});
        const__29 = RT.keyword(null, (String)"protocol");
        const__30 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__31 = (AFn)Symbol.intern(null, (String)"Startable");
        const__32 = RT.classForName((String)"datomic.connector.NotificationHandler");
        const__33 = RT.var((String)"datomic.connector", (String)"NotificationHandler");
        const__34 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"notify-sync")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"))))})), ((IObj)Symbol.intern(null, (String)"notify-data")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"))))})), ((IObj)Symbol.intern(null, (String)"notify-error")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"error"))))})), ((IObj)Symbol.intern(null, (String)"notify-db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"db"))))})), ((IObj)Symbol.intern(null, (String)"notify-index")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.connector.NotificationHandler"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.connector.NotificationHandler")});
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"notify-sync"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-sync")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"doc"), "Sync completed"}), RT.keyword(null, (String)"notify-data"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-data")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg")))), RT.keyword(null, (String)"doc"), "Transaction completed. msg has id/data/tempids/io-stats"}), RT.keyword(null, (String)"notify-error"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-error")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"error"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"error")))), RT.keyword(null, (String)"doc"), "Transaction id failed, with error"}), RT.keyword(null, (String)"notify-db"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"db"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"db")))), RT.keyword(null, (String)"doc"), "New database value. Used when catching up during connect or recovery."}), RT.keyword(null, (String)"notify-index"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-index")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Database has a new index in storage."})});
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"notify-data"), RT.keyword(null, (String)"notify-data"), RT.keyword(null, (String)"notify-db"), RT.keyword(null, (String)"notify-db"), RT.keyword(null, (String)"notify-index"), RT.keyword(null, (String)"notify-index"), RT.keyword(null, (String)"notify-error"), RT.keyword(null, (String)"notify-error"), RT.keyword(null, (String)"notify-sync"), RT.keyword(null, (String)"notify-sync")});
        const__44 = (AFn)((IObj)Symbol.intern(null, (String)"notify-sync")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"))))}));
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-sync")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"doc"), "Sync completed"});
        const__46 = (AFn)((IObj)Symbol.intern(null, (String)"notify-error")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"error"))))}));
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-error")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"error"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"error")))), RT.keyword(null, (String)"doc"), "Transaction id failed, with error"});
        const__48 = (AFn)((IObj)Symbol.intern(null, (String)"notify-index")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-index")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Database has a new index in storage."});
        const__50 = (AFn)((IObj)Symbol.intern(null, (String)"notify-db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"db"))))}));
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"db"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"db")))), RT.keyword(null, (String)"doc"), "New database value. Used when catching up during connect or recovery."});
        const__52 = (AFn)((IObj)Symbol.intern(null, (String)"notify-data")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"))))}));
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"notify-data")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg")))), RT.keyword(null, (String)"doc"), "Transaction completed. msg has id/data/tempids/io-stats"});
        const__54 = (AFn)Symbol.intern(null, (String)"NotificationHandler");
        const__55 = RT.classForName((String)"datomic.connector.TransactorConnector");
        const__56 = RT.var((String)"datomic.connector", (String)"TransactorConnector");
        const__57 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"endpoint")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), ((IObj)Symbol.intern(null, (String)"admin-request*")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"request"), (Object)Symbol.intern(null, (String)"arg"), (Object)Symbol.intern(null, (String)"timeout-msec"))))})), ((IObj)Symbol.intern(null, (String)"create-notifier")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"), (Object)Symbol.intern(null, (String)"failure-handler"))))})), ((IObj)Symbol.intern(null, (String)"start-updater")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"update-queue"), (Object)Symbol.intern(null, (String)"push-handler"), (Object)Symbol.intern(null, (String)"failure-handler"))))}))));
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.connector.TransactorConnector"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.connector.TransactorConnector")});
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"endpoint"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"endpoint")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Return the transactor endpoint or nil."}), RT.keyword(null, (String)"admin-request*"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"admin-request*")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"request"), (Object)Symbol.intern(null, (String)"arg"), (Object)Symbol.intern(null, (String)"timeout-msec"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"request"), (Object)Symbol.intern(null, (String)"arg"), (Object)Symbol.intern(null, (String)"timeout-msec")))), RT.keyword(null, (String)"doc"), "Send request to transactor. Returns map that will have :db/error and :message if error."}), RT.keyword(null, (String)"create-notifier"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"create-notifier")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"), (Object)Symbol.intern(null, (String)"failure-handler"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"), (Object)Symbol.intern(null, (String)"failure-handler")))), RT.keyword(null, (String)"doc"), "Create notifier, but do not start processing. Returns value implements Startable and AsyncShutdown"}), RT.keyword(null, (String)"start-updater"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"start-updater")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"update-queue"), (Object)Symbol.intern(null, (String)"push-handler"), (Object)Symbol.intern(null, (String)"failure-handler"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"update-queue"), (Object)Symbol.intern(null, (String)"push-handler"), (Object)Symbol.intern(null, (String)"failure-handler")))), RT.keyword(null, (String)"doc"), "Start updater, taking transactions from update-queue and queuing them to the transactor. Return value implements AsyncShutdown. Calls failure-handler with no args if update put fails. Calls push-handler (a NotificationHandler) with any errors that occur prior to remote call"})});
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"start-updater"), RT.keyword(null, (String)"start-updater"), RT.keyword(null, (String)"create-notifier"), RT.keyword(null, (String)"create-notifier"), RT.keyword(null, (String)"admin-request*"), RT.keyword(null, (String)"admin-request*"), RT.keyword(null, (String)"endpoint"), RT.keyword(null, (String)"endpoint")});
        const__66 = (AFn)((IObj)Symbol.intern(null, (String)"start-updater")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"update-queue"), (Object)Symbol.intern(null, (String)"push-handler"), (Object)Symbol.intern(null, (String)"failure-handler"))))}));
        const__67 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"start-updater")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"update-queue"), (Object)Symbol.intern(null, (String)"push-handler"), (Object)Symbol.intern(null, (String)"failure-handler"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"update-queue"), (Object)Symbol.intern(null, (String)"push-handler"), (Object)Symbol.intern(null, (String)"failure-handler")))), RT.keyword(null, (String)"doc"), "Start updater, taking transactions from update-queue and queuing them to the transactor. Return value implements AsyncShutdown. Calls failure-handler with no args if update put fails. Calls push-handler (a NotificationHandler) with any errors that occur prior to remote call"});
        const__68 = (AFn)((IObj)Symbol.intern(null, (String)"create-notifier")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"), (Object)Symbol.intern(null, (String)"failure-handler"))))}));
        const__69 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"create-notifier")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"), (Object)Symbol.intern(null, (String)"failure-handler"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"handler"), (Object)Symbol.intern(null, (String)"failure-handler")))), RT.keyword(null, (String)"doc"), "Create notifier, but do not start processing. Returns value implements Startable and AsyncShutdown"});
        const__70 = (AFn)((IObj)Symbol.intern(null, (String)"endpoint")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"endpoint")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Return the transactor endpoint or nil."});
        const__72 = (AFn)((IObj)Symbol.intern(null, (String)"admin-request*")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"request"), (Object)Symbol.intern(null, (String)"arg"), (Object)Symbol.intern(null, (String)"timeout-msec"))))}));
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"admin-request*")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"request"), (Object)Symbol.intern(null, (String)"arg"), (Object)Symbol.intern(null, (String)"timeout-msec"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"request"), (Object)Symbol.intern(null, (String)"arg"), (Object)Symbol.intern(null, (String)"timeout-msec")))), RT.keyword(null, (String)"doc"), "Send request to transactor. Returns map that will have :db/error and :message if error."});
        const__74 = (AFn)Symbol.intern(null, (String)"TransactorConnector");
        const__75 = RT.var((String)"datomic.connector", (String)"admin-request");
        const__80 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"connector"), (Object)Symbol.intern(null, (String)"request"), (Object)Symbol.intern(null, (String)"arg")), Tuple.create((Object)Symbol.intern(null, (String)"connector"), (Object)Symbol.intern(null, (String)"request"), (Object)Symbol.intern(null, (String)"arg"), (Object)Symbol.intern(null, (String)"timeout-msec")))), RT.keyword(null, (String)"column"), 1});
        const__81 = RT.var((String)"datomic.connector", (String)"endpoint-error");
        const__83 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"host"), (Object)Symbol.intern(null, (String)"alt-host"), (Object)Symbol.intern(null, (String)"port")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"endpoint")}), (Object)Symbol.intern(null, (String)"cause")))), RT.keyword(null, (String)"column"), 1});
        const__84 = RT.var((String)"datomic.connector", (String)"sfb-cache");
        const__85 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__86 = RT.var((String)"datomic.cache", (String)"create-soft-limited");
        const__87 = 10L;
        const__88 = RT.var((String)"datomic.connector", (String)"stop-all-connectors");
        const__90 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__91 = RT.var((String)"datomic.connector", (String)"try-hornet-connect");
        const__94 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"conn-factory"), (Object)Symbol.intern(null, (String)"conn-args"), (Object)Symbol.intern(null, (String)"session-args")))), RT.keyword(null, (String)"column"), 1});
        const__95 = RT.var((String)"datomic.connector", (String)"host-order");
        const__97 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"host"), (Object)Symbol.intern(null, (String)"alt-host")))), RT.keyword(null, (String)"column"), 1});
        const__98 = RT.var((String)"datomic.connector", (String)"create-hornet-factory");
    }

    public static void __init1() {
        const__100 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"host"), (Object)Symbol.intern(null, (String)"port"), (Object)Symbol.intern(null, (String)"alt-host"), (Object)Symbol.intern(null, (String)"encrypt-channel")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"endpoint")}), (Object)Symbol.intern(null, (String)"ttl")))), RT.keyword(null, (String)"column"), 1});
        const__101 = RT.var((String)"datomic.connector", (String)"notify");
        const__102 = RT.keyword(null, (String)"tx");
        const__103 = RT.keyword(null, (String)"error");
        const__104 = RT.keyword(null, (String)"index");
        const__105 = RT.keyword(null, (String)"sync");
        const__106 = RT.var((String)"datomic.connector", (String)"create-hornet-notifier");
        const__108 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"push-handler"), (Object)Symbol.intern(null, (String)"session"), (Object)Symbol.intern(null, (String)"result-queue"), (Object)Symbol.intern(null, (String)"hornet-consumer"), (Object)Symbol.intern(null, (String)"failure-handler")))), RT.keyword(null, (String)"column"), 1});
        const__109 = RT.var((String)"datomic.connector", (String)"create-transactor-hornet-connector");
        const__111 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf"), (Object)Symbol.intern(null, (String)"endpoint")), Tuple.create((Object)Symbol.intern(null, (String)"cluster-conf"), (Object)Symbol.intern(null, (String)"endpoint"), (Object)Symbol.intern(null, (String)"ttl")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        connector__init.__init0();
        connector__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.connector__init").getClassLoader());
        try {
            connector__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


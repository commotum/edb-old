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
import datomic.queue$delaying_queue;
import datomic.queue$fn__11996;
import datomic.queue$fn__12000;
import datomic.queue$fn__12003;
import datomic.queue$fn__12018;
import datomic.queue$fn__12021;
import datomic.queue$fn__12038;
import datomic.queue$fn__12041;
import datomic.queue$fn__12054;
import datomic.queue$fn__12073;
import datomic.queue$fn__12076;
import datomic.queue$fn__12087;
import datomic.queue$fn__12104;
import datomic.queue$fn__12107;
import datomic.queue$fn__12120;
import datomic.queue$fn__12122;
import datomic.queue$fn__12124;
import datomic.queue$fn__12126;
import datomic.queue$fn__12128;
import datomic.queue$fn__12131;
import datomic.queue$fn__12133;
import datomic.queue$fn__12136;
import datomic.queue$fn__12139;
import datomic.queue$fn__12141;
import datomic.queue$fn__12144;
import datomic.queue$loading__6434__auto____11994;
import datomic.queue$offer;
import datomic.queue$poll;
import datomic.queue$queue_seq;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class queue__init {
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
    public static final Object const__32;
    public static final Var const__33;
    public static final ISeq const__34;
    public static final AFn const__36;
    public static final AFn const__37;
    public static final Keyword const__38;
    public static final AFn const__39;
    public static final AFn const__40;
    public static final AFn const__41;
    public static final AFn const__42;
    public static final Object const__43;
    public static final Var const__44;
    public static final ISeq const__45;
    public static final AFn const__47;
    public static final AFn const__48;
    public static final Keyword const__49;
    public static final Keyword const__50;
    public static final AFn const__51;
    public static final AFn const__52;
    public static final AFn const__53;
    public static final AFn const__54;
    public static final AFn const__55;
    public static final AFn const__56;
    public static final Object const__57;
    public static final Var const__58;
    public static final ISeq const__59;
    public static final AFn const__61;
    public static final AFn const__62;
    public static final Keyword const__63;
    public static final Keyword const__64;
    public static final AFn const__65;
    public static final AFn const__66;
    public static final AFn const__67;
    public static final AFn const__68;
    public static final AFn const__69;
    public static final AFn const__70;
    public static final Object const__71;
    public static final Var const__72;
    public static final ISeq const__73;
    public static final AFn const__75;
    public static final AFn const__76;
    public static final Keyword const__77;
    public static final AFn const__78;
    public static final AFn const__79;
    public static final AFn const__80;
    public static final AFn const__81;
    public static final Var const__82;
    public static final AFn const__87;
    public static final Var const__88;
    public static final AFn const__90;
    public static final Var const__91;
    public static final Object const__92;
    public static final Object const__93;
    public static final Var const__94;
    public static final AFn const__96;
    public static final Var const__97;
    public static final AFn const__99;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new queue$loading__6434__auto____11994()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new queue$fn__11996())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new queue$fn__12000()).invoke();
        Object object5 = const__4;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, const__7.getRawRoot(), (Object)const__8, null);
        Object object7 = ((IFn)const__9).invoke((Object)const__6, (Object)const__10);
        Object object8 = ((IFn)const__11.getRawRoot()).invoke((Object)const__6, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__6, (Object)const__20, (Object)const__22, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__27, ((IFn)const__12.getRawRoot()).invoke((Object)const__28, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__6})))), new queue$fn__12003()})));
        Object object9 = ((IFn)const__30.getRawRoot()).invoke(const__6.getRawRoot());
        AFn aFn = const__31;
        Object object10 = ((IFn)new queue$fn__12018()).invoke();
        Object object11 = const__32;
        Object object12 = ((IFn)const__5.getRawRoot()).invoke((Object)const__33, const__7.getRawRoot(), (Object)const__8, null);
        Object object13 = ((IFn)const__9).invoke((Object)const__33, (Object)const__34);
        Object object14 = ((IFn)const__11.getRawRoot()).invoke((Object)const__33, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__36, (Object)const__17, (Object)const__37, (Object)const__19, (Object)const__33, (Object)const__20, (Object)const__39, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__40, ((IFn)const__12.getRawRoot()).invoke((Object)const__41, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__33})))), new queue$fn__12021()})));
        Object object15 = ((IFn)const__30.getRawRoot()).invoke(const__33.getRawRoot());
        AFn aFn2 = const__42;
        Object object16 = ((IFn)new queue$fn__12038()).invoke();
        Object object17 = const__43;
        Object object18 = ((IFn)const__5.getRawRoot()).invoke((Object)const__44, const__7.getRawRoot(), (Object)const__8, null);
        Object object19 = ((IFn)const__9).invoke((Object)const__44, (Object)const__45);
        Object object20 = ((IFn)const__11.getRawRoot()).invoke((Object)const__44, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__47, (Object)const__17, (Object)const__48, (Object)const__19, (Object)const__44, (Object)const__20, (Object)const__51, (Object)const__23, (Object)RT.map((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__52, ((IFn)const__12.getRawRoot()).invoke((Object)const__53, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__44})))), new queue$fn__12041(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__54, ((IFn)const__12.getRawRoot()).invoke((Object)const__55, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__44})))), new queue$fn__12054()})));
        Object object21 = ((IFn)const__30.getRawRoot()).invoke(const__44.getRawRoot());
        AFn aFn3 = const__56;
        Object object22 = ((IFn)new queue$fn__12073()).invoke();
        Object object23 = const__57;
        Object object24 = ((IFn)const__5.getRawRoot()).invoke((Object)const__58, const__7.getRawRoot(), (Object)const__8, null);
        Object object25 = ((IFn)const__9).invoke((Object)const__58, (Object)const__59);
        Object object26 = ((IFn)const__11.getRawRoot()).invoke((Object)const__58, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__61, (Object)const__17, (Object)const__62, (Object)const__19, (Object)const__58, (Object)const__20, (Object)const__65, (Object)const__23, (Object)RT.map((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__66, ((IFn)const__12.getRawRoot()).invoke((Object)const__67, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__58})))), new queue$fn__12076(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__68, ((IFn)const__12.getRawRoot()).invoke((Object)const__69, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__58})))), new queue$fn__12087()})));
        Object object27 = ((IFn)const__30.getRawRoot()).invoke(const__58.getRawRoot());
        AFn aFn4 = const__70;
        Object object28 = ((IFn)new queue$fn__12104()).invoke();
        Object object29 = const__71;
        Object object30 = ((IFn)const__5.getRawRoot()).invoke((Object)const__72, const__7.getRawRoot(), (Object)const__8, null);
        Object object31 = ((IFn)const__9).invoke((Object)const__72, (Object)const__73);
        Object object32 = ((IFn)const__11.getRawRoot()).invoke((Object)const__72, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__75, (Object)const__17, (Object)const__76, (Object)const__19, (Object)const__72, (Object)const__20, (Object)const__78, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__79, ((IFn)const__12.getRawRoot()).invoke((Object)const__80, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__72})))), new queue$fn__12107()})));
        Object object33 = ((IFn)const__30.getRawRoot()).invoke(const__72.getRawRoot());
        AFn aFn5 = const__81;
        Var var = const__82;
        var.setMeta((IPersistentMap)const__87);
        Var var2 = var;
        var.bindRoot((Object)new queue$offer());
        Var var3 = const__88;
        var3.setMeta((IPersistentMap)const__90);
        Var var4 = var3;
        var3.bindRoot((Object)new queue$poll());
        Object object34 = ((IFn)const__91.getRawRoot()).invoke(const__92, const__72.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__77, new queue$fn__12120()}), const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__21, new queue$fn__12122()}), const__44.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__49, new queue$fn__12124(), const__50, new queue$fn__12126()}), const__33.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__38, new queue$fn__12128()}), const__58.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__64, new queue$fn__12131(), const__63, new queue$fn__12133()}));
        Object object35 = ((IFn)const__91.getRawRoot()).invoke(const__93, const__33.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__38, new queue$fn__12136()}), const__58.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__64, new queue$fn__12139(), const__63, new queue$fn__12141()}));
        Object object36 = ((IFn)new queue$fn__12144()).invoke();
        Var var5 = const__94;
        var5.setMeta((IPersistentMap)const__96);
        Var var6 = var5;
        var5.bindRoot((Object)new queue$delaying_queue());
        Var var7 = const__97;
        var7.setMeta((IPersistentMap)const__99);
        Var var8 = var7;
        var7.bindRoot((Object)new queue$queue_seq());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.queue");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.classForName((String)"datomic.queue.Producer");
        const__5 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__6 = RT.var((String)"datomic.queue", (String)"Producer");
        const__7 = RT.var((String)"clojure.core", (String)"assoc");
        const__8 = RT.keyword(null, (String)"doc");
        const__9 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__10 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"offer-nb")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"))))}))));
        const__11 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.queue.Producer"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.queue.Producer")});
        const__17 = RT.keyword(null, (String)"sigs");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"offer-nb"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"offer-nb")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item")))), RT.keyword(null, (String)"doc"), "Implementaion detail. See offer."})});
        const__19 = RT.keyword(null, (String)"var");
        const__20 = RT.keyword(null, (String)"method-map");
        const__21 = RT.keyword(null, (String)"offer-nb");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"offer-nb"), RT.keyword(null, (String)"offer-nb")});
        const__23 = RT.keyword(null, (String)"method-builders");
        const__24 = RT.var((String)"clojure.core", (String)"intern");
        const__25 = RT.var((String)"clojure.core", (String)"*ns*");
        const__26 = RT.var((String)"clojure.core", (String)"with-meta");
        const__27 = (AFn)((IObj)Symbol.intern(null, (String)"offer-nb")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"))))}));
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"offer-nb")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item")))), RT.keyword(null, (String)"doc"), "Implementaion detail. See offer."});
        const__29 = RT.keyword(null, (String)"protocol");
        const__30 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__31 = (AFn)Symbol.intern(null, (String)"Producer");
        const__32 = RT.classForName((String)"datomic.queue.Consumer");
        const__33 = RT.var((String)"datomic.queue", (String)"Consumer");
        const__34 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"poll-nb")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"))))}))));
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.queue.Consumer"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.queue.Consumer")});
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"poll-nb"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"poll-nb")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else")))), RT.keyword(null, (String)"doc"), "Implementation detail. See poll."})});
        const__38 = RT.keyword(null, (String)"poll-nb");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"poll-nb"), RT.keyword(null, (String)"poll-nb")});
        const__40 = (AFn)((IObj)Symbol.intern(null, (String)"poll-nb")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"))))}));
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"poll-nb")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else")))), RT.keyword(null, (String)"doc"), "Implementation detail. See poll."});
        const__42 = (AFn)Symbol.intern(null, (String)"Consumer");
        const__43 = RT.classForName((String)"datomic.queue.BlockingProducer");
        const__44 = RT.var((String)"datomic.queue", (String)"BlockingProducer");
        const__45 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"))))})), ((IObj)Symbol.intern(null, (String)"offer-b")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"), (Object)Symbol.intern(null, (String)"msec"))))}))));
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.queue.BlockingProducer"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.queue.BlockingProducer")});
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"put"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item")))), RT.keyword(null, (String)"doc"), "Inserts item into sink, blocking until successful. Returns logical true."}), RT.keyword(null, (String)"offer-b"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"offer-b")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"), (Object)Symbol.intern(null, (String)"msec"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"), (Object)Symbol.intern(null, (String)"msec")))), RT.keyword(null, (String)"doc"), "Implementation detail, see offer."})});
        const__49 = RT.keyword(null, (String)"put");
        const__50 = RT.keyword(null, (String)"offer-b");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"put"), RT.keyword(null, (String)"put"), RT.keyword(null, (String)"offer-b"), RT.keyword(null, (String)"offer-b")});
        const__52 = (AFn)((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"))))}));
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item")))), RT.keyword(null, (String)"doc"), "Inserts item into sink, blocking until successful. Returns logical true."});
        const__54 = (AFn)((IObj)Symbol.intern(null, (String)"offer-b")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"), (Object)Symbol.intern(null, (String)"msec"))))}));
        const__55 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"offer-b")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"), (Object)Symbol.intern(null, (String)"msec"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"), (Object)Symbol.intern(null, (String)"msec")))), RT.keyword(null, (String)"doc"), "Implementation detail, see offer."});
        const__56 = (AFn)Symbol.intern(null, (String)"BlockingProducer");
        const__57 = RT.classForName((String)"datomic.queue.BlockingConsumer");
        const__58 = RT.var((String)"datomic.queue", (String)"BlockingConsumer");
        const__59 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"take")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"))))})), ((IObj)Symbol.intern(null, (String)"poll-b")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"), (Object)Symbol.intern(null, (String)"msec"))))}))));
        const__61 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.queue.BlockingConsumer"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.queue.BlockingConsumer")});
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"take"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"take")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source")))), RT.keyword(null, (String)"doc"), "Retrieves item from source, blocking until available."}), RT.keyword(null, (String)"poll-b"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"poll-b")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"), (Object)Symbol.intern(null, (String)"msec"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"), (Object)Symbol.intern(null, (String)"msec")))), RT.keyword(null, (String)"doc"), "Implementation detail, see poll."})});
        const__63 = RT.keyword(null, (String)"poll-b");
        const__64 = RT.keyword(null, (String)"take");
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"poll-b"), RT.keyword(null, (String)"poll-b"), RT.keyword(null, (String)"take"), RT.keyword(null, (String)"take")});
        const__66 = (AFn)((IObj)Symbol.intern(null, (String)"take")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"))))}));
        const__67 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"take")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source")))), RT.keyword(null, (String)"doc"), "Retrieves item from source, blocking until available."});
        const__68 = (AFn)((IObj)Symbol.intern(null, (String)"poll-b")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"), (Object)Symbol.intern(null, (String)"msec"))))}));
        const__69 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"poll-b")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"), (Object)Symbol.intern(null, (String)"msec"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"), (Object)Symbol.intern(null, (String)"msec")))), RT.keyword(null, (String)"doc"), "Implementation detail, see poll."});
        const__70 = (AFn)Symbol.intern(null, (String)"BlockingConsumer");
        const__71 = RT.classForName((String)"datomic.queue.Clear");
        const__72 = RT.var((String)"datomic.queue", (String)"Clear");
        const__73 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"clear")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"q"))))}))));
        const__75 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.queue.Clear"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.queue.Clear")});
        const__76 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"clear"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"clear")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"q"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"q")))), RT.keyword(null, (String)"doc"), "Clear all items from queue, returning queue."})});
        const__77 = RT.keyword(null, (String)"clear");
        const__78 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"clear"), RT.keyword(null, (String)"clear")});
        const__79 = (AFn)((IObj)Symbol.intern(null, (String)"clear")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"q"))))}));
        const__80 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"clear")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"q"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"q")))), RT.keyword(null, (String)"doc"), "Clear all items from queue, returning queue."});
        const__81 = (AFn)Symbol.intern(null, (String)"Clear");
        const__82 = RT.var((String)"datomic.queue", (String)"offer");
        const__87 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item")), Tuple.create((Object)Symbol.intern(null, (String)"sink"), (Object)Symbol.intern(null, (String)"item"), (Object)Symbol.intern(null, (String)"msec")))), RT.keyword(null, (String)"column"), 1});
        const__88 = RT.var((String)"datomic.queue", (String)"poll");
        const__90 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"source")), Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else")), Tuple.create((Object)Symbol.intern(null, (String)"source"), (Object)Symbol.intern(null, (String)"or-else"), (Object)Symbol.intern(null, (String)"msec")))), RT.keyword(null, (String)"column"), 1});
        const__91 = RT.var((String)"clojure.core", (String)"extend");
        const__92 = RT.classForName((String)"java.util.concurrent.BlockingQueue");
        const__93 = RT.classForName((String)"java.lang.ref.ReferenceQueue");
        const__94 = RT.var((String)"datomic.queue", (String)"delaying-queue");
        const__96 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msec"), (Object)Symbol.intern(null, (String)"dest-queue")))), RT.keyword(null, (String)"column"), 1});
        const__97 = RT.var((String)"datomic.queue", (String)"queue-seq");
        const__99 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"size")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        queue__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.queue__init").getClassLoader());
        try {
            queue__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


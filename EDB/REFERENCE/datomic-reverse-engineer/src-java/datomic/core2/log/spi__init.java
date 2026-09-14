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
package datomic.core2.log;

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
import datomic.core2.log.spi$fn__20888;
import datomic.core2.log.spi$fn__20892;
import datomic.core2.log.spi$fn__20895;
import datomic.core2.log.spi$fn__20912;
import datomic.core2.log.spi$fn__20915;
import datomic.core2.log.spi$fn__20930;
import datomic.core2.log.spi$fn__20933;
import datomic.core2.log.spi$fn__20950;
import datomic.core2.log.spi$fn__20953;
import datomic.core2.log.spi$fn__20966;
import datomic.core2.log.spi$loading__6789__auto____20886;
import datomic.core2.log.spi$normalize_scan_opts;
import datomic.core2.log.spi$result;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class spi__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Object const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Var const__8;
    public static final ISeq const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final AFn const__15;
    public static final Keyword const__16;
    public static final AFn const__17;
    public static final Keyword const__18;
    public static final Keyword const__19;
    public static final AFn const__21;
    public static final Keyword const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final AFn const__26;
    public static final AFn const__27;
    public static final Keyword const__28;
    public static final Var const__29;
    public static final AFn const__30;
    public static final Object const__31;
    public static final Var const__32;
    public static final ISeq const__33;
    public static final AFn const__35;
    public static final AFn const__36;
    public static final AFn const__38;
    public static final AFn const__39;
    public static final AFn const__40;
    public static final AFn const__41;
    public static final Object const__42;
    public static final Var const__43;
    public static final ISeq const__44;
    public static final AFn const__46;
    public static final AFn const__47;
    public static final AFn const__49;
    public static final AFn const__50;
    public static final AFn const__51;
    public static final AFn const__52;
    public static final Object const__53;
    public static final Var const__54;
    public static final ISeq const__55;
    public static final AFn const__57;
    public static final AFn const__58;
    public static final AFn const__61;
    public static final AFn const__62;
    public static final AFn const__63;
    public static final AFn const__64;
    public static final AFn const__65;
    public static final AFn const__66;
    public static final Var const__67;
    public static final AFn const__72;
    public static final Var const__73;
    public static final AFn const__75;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new spi$loading__6789__auto____20886()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new spi$fn__20888())));
            v2 = null;
        }
        Object object3 = ((IFn)new spi$fn__20892()).invoke();
        Object object4 = const__3;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, const__6.getRawRoot(), (Object)const__7, null);
        Object object6 = ((IFn)const__8).invoke((Object)const__5, (Object)const__9);
        Object object7 = ((IFn)const__10.getRawRoot()).invoke((Object)const__5, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__5, (Object)const__19, (Object)const__21, (Object)const__22, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__26, ((IFn)const__11.getRawRoot()).invoke((Object)const__27, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__5})))), new spi$fn__20895()})));
        Object object8 = ((IFn)const__29.getRawRoot()).invoke(const__5.getRawRoot());
        AFn aFn = const__30;
        Object object9 = ((IFn)new spi$fn__20912()).invoke();
        Object object10 = const__31;
        Object object11 = ((IFn)const__4.getRawRoot()).invoke((Object)const__32, const__6.getRawRoot(), (Object)const__7, null);
        Object object12 = ((IFn)const__8).invoke((Object)const__32, (Object)const__33);
        Object object13 = ((IFn)const__10.getRawRoot()).invoke((Object)const__32, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__35, (Object)const__16, (Object)const__36, (Object)const__18, (Object)const__32, (Object)const__19, (Object)const__38, (Object)const__22, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__39, ((IFn)const__11.getRawRoot()).invoke((Object)const__40, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__32})))), new spi$fn__20915()})));
        Object object14 = ((IFn)const__29.getRawRoot()).invoke(const__32.getRawRoot());
        AFn aFn2 = const__41;
        Object object15 = ((IFn)new spi$fn__20930()).invoke();
        Object object16 = const__42;
        Object object17 = ((IFn)const__4.getRawRoot()).invoke((Object)const__43, const__6.getRawRoot(), (Object)const__7, null);
        Object object18 = ((IFn)const__8).invoke((Object)const__43, (Object)const__44);
        Object object19 = ((IFn)const__10.getRawRoot()).invoke((Object)const__43, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__46, (Object)const__16, (Object)const__47, (Object)const__18, (Object)const__43, (Object)const__19, (Object)const__49, (Object)const__22, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__50, ((IFn)const__11.getRawRoot()).invoke((Object)const__51, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__43})))), new spi$fn__20933()})));
        Object object20 = ((IFn)const__29.getRawRoot()).invoke(const__43.getRawRoot());
        AFn aFn3 = const__52;
        Object object21 = ((IFn)new spi$fn__20950()).invoke();
        Object object22 = const__53;
        Object object23 = ((IFn)const__4.getRawRoot()).invoke((Object)const__54, const__6.getRawRoot(), (Object)const__7, null);
        Object object24 = ((IFn)const__8).invoke((Object)const__54, (Object)const__55);
        Object object25 = ((IFn)const__10.getRawRoot()).invoke((Object)const__54, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__57, (Object)const__16, (Object)const__58, (Object)const__18, (Object)const__54, (Object)const__19, (Object)const__61, (Object)const__22, (Object)RT.map((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__62, ((IFn)const__11.getRawRoot()).invoke((Object)const__63, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__54})))), new spi$fn__20953(), ((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__64, ((IFn)const__11.getRawRoot()).invoke((Object)const__65, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__54})))), new spi$fn__20966()})));
        Object object26 = ((IFn)const__29.getRawRoot()).invoke(const__54.getRawRoot());
        AFn aFn4 = const__66;
        Var var = const__67;
        var.setMeta((IPersistentMap)const__72);
        Var var2 = var;
        var.bindRoot((Object)new spi$result());
        Var var3 = const__73;
        var3.setMeta((IPersistentMap)const__75);
        Var var4 = var3;
        var3.bindRoot((Object)new spi$normalize_scan_opts());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.log.spi");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.classForName((String)"datomic.core2.log.spi.Append");
        const__4 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__5 = RT.var((String)"datomic.core2.log.spi", (String)"Append");
        const__6 = RT.var((String)"clojure.core", (String)"assoc");
        const__7 = RT.keyword(null, (String)"doc");
        const__8 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__9 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-append")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"header"), (Object)Symbol.intern(null, (String)"body"))))}))));
        const__10 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__11 = RT.var((String)"clojure.core", (String)"merge");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.log.spi.Append"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.log.spi.Append")});
        const__16 = RT.keyword(null, (String)"sigs");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-append"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-append")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"header"), (Object)Symbol.intern(null, (String)"body"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"header"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.log/append."})});
        const__18 = RT.keyword(null, (String)"var");
        const__19 = RT.keyword(null, (String)"method-map");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-append"), RT.keyword(null, (String)"-append")});
        const__22 = RT.keyword(null, (String)"method-builders");
        const__23 = RT.var((String)"clojure.core", (String)"intern");
        const__24 = RT.var((String)"clojure.core", (String)"*ns*");
        const__25 = RT.var((String)"clojure.core", (String)"with-meta");
        const__26 = (AFn)((IObj)Symbol.intern(null, (String)"-append")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"header"), (Object)Symbol.intern(null, (String)"body"))))}));
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-append")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"header"), (Object)Symbol.intern(null, (String)"body"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"header"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.log/append."});
        const__28 = RT.keyword(null, (String)"protocol");
        const__29 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__30 = (AFn)Symbol.intern(null, (String)"Append");
        const__31 = RT.classForName((String)"datomic.core2.log.spi.Delete");
        const__32 = RT.var((String)"datomic.core2.log.spi", (String)"Delete");
        const__33 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"t"))))}))));
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.log.spi.Delete"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.log.spi.Delete")});
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-delete"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"t"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"t")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.log/delete."})});
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-delete"), RT.keyword(null, (String)"-delete")});
        const__39 = (AFn)((IObj)Symbol.intern(null, (String)"-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"t"))))}));
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"t"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"t")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.log/delete."});
        const__41 = (AFn)Symbol.intern(null, (String)"Delete");
        const__42 = RT.classForName((String)"datomic.core2.log.spi.Scan");
        const__43 = RT.var((String)"datomic.core2.log.spi", (String)"Scan");
        const__44 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-scan")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"opts"))))}))));
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.log.spi.Scan"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.log.spi.Scan")});
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-scan"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-scan")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "SPI for datomc.core2.log/scan. Return value ignored."})});
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-scan"), RT.keyword(null, (String)"-scan")});
        const__50 = (AFn)((IObj)Symbol.intern(null, (String)"-scan")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-scan")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "SPI for datomc.core2.log/scan. Return value ignored."});
        const__52 = (AFn)Symbol.intern(null, (String)"Scan");
        const__53 = RT.classForName((String)"datomic.core2.log.spi.Item");
        const__54 = RT.var((String)"datomic.core2.log.spi", (String)"Item");
        const__55 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-item-header")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item"))))})), ((IObj)Symbol.intern(null, (String)"-item-body")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item"))))}))));
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.log.spi.Item"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.log.spi.Item")});
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-item-header"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-item-header")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.log/item-header."}), RT.keyword(null, (String)"-item-body"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-item-body")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.log/item-body."})});
        const__61 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-item-body"), RT.keyword(null, (String)"-item-body"), RT.keyword(null, (String)"-item-header"), RT.keyword(null, (String)"-item-header")});
        const__62 = (AFn)((IObj)Symbol.intern(null, (String)"-item-header")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item"))))}));
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-item-header")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.log/item-header."});
        const__64 = (AFn)((IObj)Symbol.intern(null, (String)"-item-body")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item"))))}));
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-item-body")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"item")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.log/item-body."});
        const__66 = (AFn)Symbol.intern(null, (String)"Item");
        const__67 = RT.var((String)"datomic.core2.log.spi", (String)"result");
        const__72 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"column"), 1});
        const__73 = RT.var((String)"datomic.core2.log.spi", (String)"normalize-scan-opts");
        const__75 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"direction"), (Object)Symbol.intern(null, (String)"t"), (Object)Symbol.intern(null, (String)"ch"), (Object)Symbol.intern(null, (String)"limit")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"opts")})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        spi__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.log.spi__init").getClassLoader());
        try {
            spi__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


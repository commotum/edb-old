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
package datomic.core2.val_store;

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
import datomic.core2.val_store.spi$fn__21785;
import datomic.core2.val_store.spi$fn__21789;
import datomic.core2.val_store.spi$fn__21792;
import datomic.core2.val_store.spi$fn__21811;
import datomic.core2.val_store.spi$fn__21814;
import datomic.core2.val_store.spi$fn__21831;
import datomic.core2.val_store.spi$fn__21834;
import datomic.core2.val_store.spi$loading__6789__auto____21783;
import datomic.core2.val_store.spi$no_val_error;
import datomic.core2.val_store.spi$partition_key;
import datomic.core2.val_store.spi$splice_partition_key;
import datomic.core2.val_store.spi$val_op_succeeded_QMARK_;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class spi__init {
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
    public static final AFn const__39;
    public static final AFn const__40;
    public static final AFn const__41;
    public static final AFn const__42;
    public static final Object const__43;
    public static final Var const__44;
    public static final ISeq const__45;
    public static final AFn const__47;
    public static final AFn const__48;
    public static final AFn const__50;
    public static final AFn const__51;
    public static final AFn const__52;
    public static final AFn const__53;
    public static final Var const__54;
    public static final AFn const__59;
    public static final Var const__60;
    public static final AFn const__62;
    public static final Var const__63;
    public static final AFn const__65;
    public static final Var const__66;
    public static final AFn const__68;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new spi$loading__6789__auto____21783()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new spi$fn__21785())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new spi$fn__21789()).invoke();
        Object object5 = const__4;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, const__7.getRawRoot(), (Object)const__8, null);
        Object object7 = ((IFn)const__9).invoke((Object)const__6, (Object)const__10);
        Object object8 = ((IFn)const__11.getRawRoot()).invoke((Object)const__6, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__6, (Object)const__20, (Object)const__22, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__27, ((IFn)const__12.getRawRoot()).invoke((Object)const__28, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__6})))), new spi$fn__21792()})));
        Object object9 = ((IFn)const__30.getRawRoot()).invoke(const__6.getRawRoot());
        AFn aFn = const__31;
        Object object10 = ((IFn)new spi$fn__21811()).invoke();
        Object object11 = const__32;
        Object object12 = ((IFn)const__5.getRawRoot()).invoke((Object)const__33, const__7.getRawRoot(), (Object)const__8, null);
        Object object13 = ((IFn)const__9).invoke((Object)const__33, (Object)const__34);
        Object object14 = ((IFn)const__11.getRawRoot()).invoke((Object)const__33, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__36, (Object)const__17, (Object)const__37, (Object)const__19, (Object)const__33, (Object)const__20, (Object)const__39, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__40, ((IFn)const__12.getRawRoot()).invoke((Object)const__41, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__33})))), new spi$fn__21814()})));
        Object object15 = ((IFn)const__30.getRawRoot()).invoke(const__33.getRawRoot());
        AFn aFn2 = const__42;
        Object object16 = ((IFn)new spi$fn__21831()).invoke();
        Object object17 = const__43;
        Object object18 = ((IFn)const__5.getRawRoot()).invoke((Object)const__44, const__7.getRawRoot(), (Object)const__8, null);
        Object object19 = ((IFn)const__9).invoke((Object)const__44, (Object)const__45);
        Object object20 = ((IFn)const__11.getRawRoot()).invoke((Object)const__44, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__47, (Object)const__17, (Object)const__48, (Object)const__19, (Object)const__44, (Object)const__20, (Object)const__50, (Object)const__23, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__51, ((IFn)const__12.getRawRoot()).invoke((Object)const__52, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__44})))), new spi$fn__21834()})));
        Object object21 = ((IFn)const__30.getRawRoot()).invoke(const__44.getRawRoot());
        AFn aFn3 = const__53;
        Var var = const__54;
        var.setMeta((IPersistentMap)const__59);
        Var var2 = var;
        var.bindRoot((Object)new spi$no_val_error());
        Var var3 = const__60;
        var3.setMeta((IPersistentMap)const__62);
        Var var4 = var3;
        var3.bindRoot((Object)new spi$partition_key());
        Var var5 = const__63;
        var5.setMeta((IPersistentMap)const__65);
        Var var6 = var5;
        var5.bindRoot((Object)new spi$splice_partition_key());
        Var var7 = const__66;
        var7.setMeta((IPersistentMap)const__68);
        Var var8 = var7;
        var7.bindRoot((Object)new spi$val_op_succeeded_QMARK_());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.val-store.spi");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.classForName((String)"datomic.core2.val_store.spi.Put");
        const__5 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__6 = RT.var((String)"datomic.core2.val-store.spi", (String)"Put");
        const__7 = RT.var((String)"clojure.core", (String)"assoc");
        const__8 = RT.keyword(null, (String)"doc");
        const__9 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__10 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))}))));
        const__11 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.val_store.spi.Put"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.val_store.spi.Put")});
        const__17 = RT.keyword(null, (String)"sigs");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-put"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.val-store/put."})});
        const__19 = RT.keyword(null, (String)"var");
        const__20 = RT.keyword(null, (String)"method-map");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-put"), RT.keyword(null, (String)"-put")});
        const__23 = RT.keyword(null, (String)"method-builders");
        const__24 = RT.var((String)"clojure.core", (String)"intern");
        const__25 = RT.var((String)"clojure.core", (String)"*ns*");
        const__26 = RT.var((String)"clojure.core", (String)"with-meta");
        const__27 = (AFn)((IObj)Symbol.intern(null, (String)"-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.val-store/put."});
        const__29 = RT.keyword(null, (String)"protocol");
        const__30 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__31 = (AFn)Symbol.intern(null, (String)"Put");
        const__32 = RT.classForName((String)"datomic.core2.val_store.spi.Get");
        const__33 = RT.var((String)"datomic.core2.val-store.spi", (String)"Get");
        const__34 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))}))));
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.val_store.spi.Get"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.val_store.spi.Get")});
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-get"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.val-store/get."})});
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-get"), RT.keyword(null, (String)"-get")});
        const__40 = (AFn)((IObj)Symbol.intern(null, (String)"-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.val-store/get."});
        const__42 = (AFn)Symbol.intern(null, (String)"Get");
        const__43 = RT.classForName((String)"datomic.core2.val_store.spi.Delete");
        const__44 = RT.var((String)"datomic.core2.val-store.spi", (String)"Delete");
        const__45 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))}))));
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.val_store.spi.Delete"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.val_store.spi.Delete")});
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-delete"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.val-store/delete."})});
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-delete"), RT.keyword(null, (String)"-delete")});
        const__51 = (AFn)((IObj)Symbol.intern(null, (String)"-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "SPI for datomic.core2.val-store/delete."});
        const__53 = (AFn)Symbol.intern(null, (String)"Delete");
        const__54 = RT.var((String)"datomic.core2.val-store.spi", (String)"no-val-error");
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"column"), 1});
        const__60 = RT.var((String)"datomic.core2.val-store.spi", (String)"partition-key");
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)((IObj)Symbol.intern(null, (String)"s")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.String")})))), RT.keyword(null, (String)"column"), 1});
        const__63 = RT.var((String)"datomic.core2.val-store.spi", (String)"splice-partition-key");
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"pk")))), RT.keyword(null, (String)"column"), 1});
        const__66 = RT.var((String)"datomic.core2.val-store.spi", (String)"val-op-succeeded?");
        const__68 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"store-api-result")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        spi__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.val_store.spi__init").getClassLoader());
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


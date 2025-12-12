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
package datomic.core2.val_store.s3;

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
import datomic.core2.val_store.s3.sdkv1$create;
import datomic.core2.val_store.s3.sdkv1$fn__21676;
import datomic.core2.val_store.s3.sdkv1$fn__21686;
import datomic.core2.val_store.s3.sdkv1$fn__21689;
import datomic.core2.val_store.s3.sdkv1$fn__21704;
import datomic.core2.val_store.s3.sdkv1$fn__21721;
import datomic.core2.val_store.s3.sdkv1$fn__21742;
import datomic.core2.val_store.s3.sdkv1$fn__21761;
import datomic.core2.val_store.s3.sdkv1$loading__6789__auto____21674;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class sdkv1__init {
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
    public static final AFn const__25;
    public static final Keyword const__26;
    public static final Var const__27;
    public static final Var const__28;
    public static final Var const__29;
    public static final AFn const__30;
    public static final AFn const__31;
    public static final Keyword const__32;
    public static final AFn const__33;
    public static final AFn const__34;
    public static final AFn const__35;
    public static final AFn const__36;
    public static final AFn const__37;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__46;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new sdkv1$loading__6789__auto____21674()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new sdkv1$fn__21676())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new sdkv1$fn__21686()).invoke();
        Object object5 = const__4;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, const__7.getRawRoot(), (Object)const__8, null);
        Object object7 = ((IFn)const__9).invoke((Object)const__6, (Object)const__10);
        Object object8 = ((IFn)const__11.getRawRoot()).invoke((Object)const__6, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__6, (Object)const__20, (Object)const__25, (Object)const__26, (Object)RT.map((Object[])new Object[]{((IFn)const__27.getRawRoot()).invoke(const__28.get(), ((IFn)const__29.getRawRoot()).invoke((Object)const__30, ((IFn)const__12.getRawRoot()).invoke((Object)const__31, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__32, const__6})))), new sdkv1$fn__21689(), ((IFn)const__27.getRawRoot()).invoke(const__28.get(), ((IFn)const__29.getRawRoot()).invoke((Object)const__33, ((IFn)const__12.getRawRoot()).invoke((Object)const__34, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__32, const__6})))), new sdkv1$fn__21704(), ((IFn)const__27.getRawRoot()).invoke(const__28.get(), ((IFn)const__29.getRawRoot()).invoke((Object)const__35, ((IFn)const__12.getRawRoot()).invoke((Object)const__36, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__32, const__6})))), new sdkv1$fn__21721(), ((IFn)const__27.getRawRoot()).invoke(const__28.get(), ((IFn)const__29.getRawRoot()).invoke((Object)const__37, ((IFn)const__12.getRawRoot()).invoke((Object)const__38, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__32, const__6})))), new sdkv1$fn__21742()})));
        Object object9 = ((IFn)const__39.getRawRoot()).invoke(const__6.getRawRoot());
        AFn aFn = const__40;
        Object object10 = ((IFn)new sdkv1$fn__21761()).invoke();
        Var var = const__41;
        var.setMeta((IPersistentMap)const__46);
        Var var2 = var;
        var.bindRoot((Object)new sdkv1$create());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.val-store.s3.sdkv1");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.classForName((String)"datomic.core2.val_store.s3.sdkv1.Impl");
        const__5 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__6 = RT.var((String)"datomic.core2.val-store.s3.sdkv1", (String)"Impl");
        const__7 = RT.var((String)"clojure.core", (String)"assoc");
        const__8 = RT.keyword(null, (String)"doc");
        const__9 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__10 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-sync-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), ((IObj)Symbol.intern(null, (String)"-sync-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))})), ((IObj)Symbol.intern(null, (String)"-sync-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), ((IObj)Symbol.intern(null, (String)"-wrap-op")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op"), (Object)Symbol.intern(null, (String)"opts"), (Object)Symbol.intern(null, (String)"context"))))}))));
        const__11 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.val_store.s3.sdkv1.Impl"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.val_store.s3.sdkv1.Impl")});
        const__17 = RT.keyword(null, (String)"sigs");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-sync-get"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:val ByteBuffer (or nil iff not found)}  or anomaly."}), RT.keyword(null, (String)"-sync-put"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:result :created} or anomaly."}), RT.keyword(null, (String)"-sync-delete"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:result :deleted} or anomaly."}), RT.keyword(null, (String)"-wrap-op"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-wrap-op")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op"), (Object)Symbol.intern(null, (String)"opts"), (Object)Symbol.intern(null, (String)"context"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op"), (Object)Symbol.intern(null, (String)"opts"), (Object)Symbol.intern(null, (String)"context")))), RT.keyword(null, (String)"doc"), "Wraps f in a retry, metric-handler, and exception->anom handler. Returns (f) or anomaly."})});
        const__19 = RT.keyword(null, (String)"var");
        const__20 = RT.keyword(null, (String)"method-map");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-sync-put"), RT.keyword(null, (String)"-sync-put"), RT.keyword(null, (String)"-wrap-op"), RT.keyword(null, (String)"-wrap-op"), RT.keyword(null, (String)"-sync-delete"), RT.keyword(null, (String)"-sync-delete"), RT.keyword(null, (String)"-sync-get"), RT.keyword(null, (String)"-sync-get")});
        const__26 = RT.keyword(null, (String)"method-builders");
        const__27 = RT.var((String)"clojure.core", (String)"intern");
        const__28 = RT.var((String)"clojure.core", (String)"*ns*");
        const__29 = RT.var((String)"clojure.core", (String)"with-meta");
        const__30 = (AFn)((IObj)Symbol.intern(null, (String)"-sync-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:val ByteBuffer (or nil iff not found)}  or anomaly."});
        const__32 = RT.keyword(null, (String)"protocol");
        const__33 = (AFn)((IObj)Symbol.intern(null, (String)"-sync-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:result :created} or anomaly."});
        const__35 = (AFn)((IObj)Symbol.intern(null, (String)"-wrap-op")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op"), (Object)Symbol.intern(null, (String)"opts"), (Object)Symbol.intern(null, (String)"context"))))}));
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-wrap-op")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op"), (Object)Symbol.intern(null, (String)"opts"), (Object)Symbol.intern(null, (String)"context"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op"), (Object)Symbol.intern(null, (String)"opts"), (Object)Symbol.intern(null, (String)"context")))), RT.keyword(null, (String)"doc"), "Wraps f in a retry, metric-handler, and exception->anom handler. Returns (f) or anomaly."});
        const__37 = (AFn)((IObj)Symbol.intern(null, (String)"-sync-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:result :deleted} or anomaly."});
        const__39 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__40 = (AFn)Symbol.intern(null, (String)"Impl");
        const__41 = RT.var((String)"datomic.core2.val-store.s3.sdkv1", (String)"create");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"read-pool"), (Object)Symbol.intern(null, (String)"write-pool"), (Object)Symbol.intern(null, (String)"retry-fn"), (Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"prefix"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        sdkv1__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.val_store.s3.sdkv1__init").getClassLoader());
        try {
            sdkv1__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


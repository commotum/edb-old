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
 *  clojure.lang.PersistentVector
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
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.val_store.fs$create;
import datomic.core2.val_store.fs$fn__21257;
import datomic.core2.val_store.fs$fn__21271;
import datomic.core2.val_store.fs$fn__21274;
import datomic.core2.val_store.fs$fn__21289;
import datomic.core2.val_store.fs$fn__21304;
import datomic.core2.val_store.fs$fn__21324;
import datomic.core2.val_store.fs$loading__6789__auto____21255;
import datomic.core2.val_store.fs$wrap_metric_handler;
import datomic.core2.val_store.fs$wrap_op;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class fs__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__7;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__16;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__27;
    public static final Object const__28;
    public static final Var const__29;
    public static final Var const__30;
    public static final Var const__31;
    public static final Keyword const__32;
    public static final Var const__33;
    public static final ISeq const__34;
    public static final Var const__35;
    public static final Var const__36;
    public static final AFn const__40;
    public static final Keyword const__41;
    public static final AFn const__42;
    public static final Keyword const__43;
    public static final Keyword const__44;
    public static final AFn const__48;
    public static final Keyword const__49;
    public static final Var const__50;
    public static final Var const__51;
    public static final Var const__52;
    public static final AFn const__53;
    public static final AFn const__54;
    public static final Keyword const__55;
    public static final AFn const__56;
    public static final AFn const__57;
    public static final AFn const__58;
    public static final AFn const__59;
    public static final Var const__60;
    public static final AFn const__61;
    public static final Var const__62;
    public static final AFn const__63;
    public static final Var const__64;
    public static final Object const__65;
    public static final Var const__66;
    public static final AFn const__67;
    public static final Var const__68;
    public static final AFn const__69;
    public static final Object const__70;
    public static final Var const__71;
    public static final AFn const__73;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new fs$loading__6789__auto____21255()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new fs$fn__21257())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot((Object)const__14);
        Var var3 = const__15;
        var3.setMeta((IPersistentMap)const__16);
        Var var4 = var3;
        var3.bindRoot((Object)const__20);
        Var var5 = const__21;
        var5.setMeta((IPersistentMap)const__24);
        Var var6 = var5;
        var5.bindRoot((Object)new fs$wrap_metric_handler());
        Var var7 = const__25;
        var7.setMeta((IPersistentMap)const__27);
        Var var8 = var7;
        var7.bindRoot((Object)new fs$wrap_op());
        Object object4 = ((IFn)new fs$fn__21271()).invoke();
        Object object5 = const__28;
        Object object6 = ((IFn)const__29.getRawRoot()).invoke((Object)const__30, const__31.getRawRoot(), (Object)const__32, null);
        Object object7 = ((IFn)const__33).invoke((Object)const__30, (Object)const__34);
        Object object8 = ((IFn)const__35.getRawRoot()).invoke((Object)const__30, const__36.getRawRoot(), ((IFn)const__31.getRawRoot()).invoke((Object)const__40, (Object)const__41, (Object)const__42, (Object)const__43, (Object)const__30, (Object)const__44, (Object)const__48, (Object)const__49, (Object)RT.map((Object[])new Object[]{((IFn)const__50.getRawRoot()).invoke(const__51.get(), ((IFn)const__52.getRawRoot()).invoke((Object)const__53, ((IFn)const__36.getRawRoot()).invoke((Object)const__54, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__55, const__30})))), new fs$fn__21274(), ((IFn)const__50.getRawRoot()).invoke(const__51.get(), ((IFn)const__52.getRawRoot()).invoke((Object)const__56, ((IFn)const__36.getRawRoot()).invoke((Object)const__57, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__55, const__30})))), new fs$fn__21289(), ((IFn)const__50.getRawRoot()).invoke(const__51.get(), ((IFn)const__52.getRawRoot()).invoke((Object)const__58, ((IFn)const__36.getRawRoot()).invoke((Object)const__59, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__55, const__30})))), new fs$fn__21304()})));
        Object object9 = ((IFn)const__60.getRawRoot()).invoke(const__30.getRawRoot());
        AFn aFn = const__61;
        Var var9 = const__62;
        var9.setMeta((IPersistentMap)const__63);
        Var var10 = var9;
        var9.bindRoot(((IFn)const__64.getRawRoot()).invoke(const__65, (Object)Tuple.create((Object)StandardOpenOption.TRUNCATE_EXISTING, (Object)StandardOpenOption.WRITE, (Object)StandardOpenOption.CREATE)));
        Var var11 = const__66;
        var11.setMeta((IPersistentMap)const__67);
        Var var12 = var11;
        var11.bindRoot(((IFn)const__64.getRawRoot()).invoke(const__65, (Object)Tuple.create((Object)StandardOpenOption.READ)));
        Var var13 = const__68;
        var13.setMeta((IPersistentMap)const__69);
        Var var14 = var13;
        var13.bindRoot(((IFn)const__64.getRawRoot()).invoke(const__70, (Object)PersistentVector.EMPTY));
        Object object10 = ((IFn)new fs$fn__21324()).invoke();
        Var var15 = const__71;
        var15.setMeta((IPersistentMap)const__73);
        Var var16 = var15;
        var15.bindRoot((Object)new fs$create());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.val-store.fs");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.core2.val-store.fs", (String)"success-metrics");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"get"), RT.keyword(null, (String)"efs.get.succeeded.msec"), RT.keyword(null, (String)"put"), RT.keyword(null, (String)"efs.put.succeeded.msec"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"efs.delete.succeeded.msec")});
        const__15 = RT.var((String)"datomic.core2.val-store.fs", (String)"failure-metrics");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"get"), RT.keyword(null, (String)"efs.get.failed.msec"), RT.keyword(null, (String)"put"), RT.keyword(null, (String)"efs.put.failed.msec"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"efs.delete.failed.msec")});
        const__21 = RT.var((String)"datomic.core2.val-store.fs", (String)"wrap-metric-handler");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.core2.val-store.fs", (String)"wrap-op");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"op")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.classForName((String)"datomic.core2.val_store.fs.Impl");
        const__29 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__30 = RT.var((String)"datomic.core2.val-store.fs", (String)"Impl");
        const__31 = RT.var((String)"clojure.core", (String)"assoc");
        const__32 = RT.keyword(null, (String)"doc");
        const__33 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__34 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-file-path")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), ((IObj)Symbol.intern(null, (String)"-sync-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), ((IObj)Symbol.intern(null, (String)"-sync-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))}))));
        const__35 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__36 = RT.var((String)"clojure.core", (String)"merge");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.val_store.fs.Impl"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.val_store.fs.Impl")});
        const__41 = RT.keyword(null, (String)"sigs");
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-file-path"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-file-path")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), null}), RT.keyword(null, (String)"-sync-get"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:val ByteBuffer (or nil iff not found)}  or anomaly."}), RT.keyword(null, (String)"-sync-put"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:result :created} or anomaly."})});
        const__43 = RT.keyword(null, (String)"var");
        const__44 = RT.keyword(null, (String)"method-map");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-sync-put"), RT.keyword(null, (String)"-sync-put"), RT.keyword(null, (String)"-sync-get"), RT.keyword(null, (String)"-sync-get"), RT.keyword(null, (String)"-file-path"), RT.keyword(null, (String)"-file-path")});
        const__49 = RT.keyword(null, (String)"method-builders");
        const__50 = RT.var((String)"clojure.core", (String)"intern");
        const__51 = RT.var((String)"clojure.core", (String)"*ns*");
        const__52 = RT.var((String)"clojure.core", (String)"with-meta");
        const__53 = (AFn)((IObj)Symbol.intern(null, (String)"-sync-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:val ByteBuffer (or nil iff not found)}  or anomaly."});
        const__55 = RT.keyword(null, (String)"protocol");
        const__56 = (AFn)((IObj)Symbol.intern(null, (String)"-file-path")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-file-path")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), null});
        const__58 = (AFn)((IObj)Symbol.intern(null, (String)"-sync-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))}));
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-sync-put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"doc"), "Returns {:result :created} or anomaly."});
        const__60 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__61 = (AFn)Symbol.intern(null, (String)"Impl");
        const__62 = RT.var((String)"datomic.core2.val-store.fs", (String)"SYNC_PUT_OPEN_OPTIONS");
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__64 = RT.var((String)"clojure.core", (String)"into-array");
        const__65 = RT.classForName((String)"java.nio.file.StandardOpenOption");
        const__66 = RT.var((String)"datomic.core2.val-store.fs", (String)"SYNC_GET_READ_OPTION");
        const__67 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__68 = RT.var((String)"datomic.core2.val-store.fs", (String)"CREATE_DIRECTORIES_OPTS");
        const__69 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__70 = RT.classForName((String)"java.nio.file.attribute.FileAttribute");
        const__71 = RT.var((String)"datomic.core2.val-store.fs", (String)"create");
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"delete-pool"), (Object)Symbol.intern(null, (String)"get-pool"), (Object)Symbol.intern(null, (String)"path"), (Object)Symbol.intern(null, (String)"put-pool"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        fs__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.val_store.fs__init").getClassLoader());
        try {
            fs__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


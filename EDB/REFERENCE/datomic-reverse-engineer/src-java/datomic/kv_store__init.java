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
import datomic.kv_store$fn__10748;
import datomic.kv_store$fn__10758;
import datomic.kv_store$fn__10761;
import datomic.kv_store$fn__10776;
import datomic.kv_store$fn__10791;
import datomic.kv_store$fn__10804;
import datomic.kv_store$fn__10817;
import datomic.kv_store$fn__10820;
import datomic.kv_store$fn__10831;
import datomic.kv_store$fn__10833;
import datomic.kv_store$loading__6434__auto____10746;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class kv_store__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__7;
    public static final Object const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Var const__11;
    public static final Keyword const__12;
    public static final Var const__13;
    public static final ISeq const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final AFn const__20;
    public static final Keyword const__21;
    public static final AFn const__22;
    public static final Keyword const__23;
    public static final Keyword const__24;
    public static final AFn const__29;
    public static final Keyword const__30;
    public static final Var const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final AFn const__34;
    public static final AFn const__35;
    public static final Keyword const__36;
    public static final AFn const__37;
    public static final AFn const__38;
    public static final AFn const__39;
    public static final AFn const__40;
    public static final AFn const__41;
    public static final AFn const__42;
    public static final Var const__43;
    public static final AFn const__44;
    public static final Object const__45;
    public static final Var const__46;
    public static final ISeq const__47;
    public static final AFn const__49;
    public static final AFn const__50;
    public static final Keyword const__51;
    public static final AFn const__52;
    public static final AFn const__53;
    public static final AFn const__54;
    public static final AFn const__55;
    public static final Var const__56;
    public static final Object const__57;
    public static final Object const__58;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new kv_store$loading__6434__auto____10746()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new kv_store$fn__10748())));
            v2 = null;
        }
        Var var = const__3.setDynamic(true);
        Var var2 = var;
        var.setMeta((IPersistentMap)const__7);
        Object object3 = ((IFn)new kv_store$fn__10758()).invoke();
        Object object4 = const__8;
        Object object5 = ((IFn)const__9.getRawRoot()).invoke((Object)const__10, const__11.getRawRoot(), (Object)const__12, null);
        Object object6 = ((IFn)const__13).invoke((Object)const__10, (Object)const__14);
        Object object7 = ((IFn)const__15.getRawRoot()).invoke((Object)const__10, const__16.getRawRoot(), ((IFn)const__11.getRawRoot()).invoke((Object)const__20, (Object)const__21, (Object)const__22, (Object)const__23, (Object)const__10, (Object)const__24, (Object)const__29, (Object)const__30, (Object)RT.map((Object[])new Object[]{((IFn)const__31.getRawRoot()).invoke(const__32.get(), ((IFn)const__33.getRawRoot()).invoke((Object)const__34, ((IFn)const__16.getRawRoot()).invoke((Object)const__35, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__36, const__10})))), new kv_store$fn__10761(), ((IFn)const__31.getRawRoot()).invoke(const__32.get(), ((IFn)const__33.getRawRoot()).invoke((Object)const__37, ((IFn)const__16.getRawRoot()).invoke((Object)const__38, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__36, const__10})))), new kv_store$fn__10776(), ((IFn)const__31.getRawRoot()).invoke(const__32.get(), ((IFn)const__33.getRawRoot()).invoke((Object)const__39, ((IFn)const__16.getRawRoot()).invoke((Object)const__40, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__36, const__10})))), new kv_store$fn__10791(), ((IFn)const__31.getRawRoot()).invoke(const__32.get(), ((IFn)const__33.getRawRoot()).invoke((Object)const__41, ((IFn)const__16.getRawRoot()).invoke((Object)const__42, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__36, const__10})))), new kv_store$fn__10804()})));
        Object object8 = ((IFn)const__43.getRawRoot()).invoke(const__10.getRawRoot());
        AFn aFn = const__44;
        Object object9 = ((IFn)new kv_store$fn__10817()).invoke();
        Object object10 = const__45;
        Object object11 = ((IFn)const__9.getRawRoot()).invoke((Object)const__46, const__11.getRawRoot(), (Object)const__12, null);
        Object object12 = ((IFn)const__13).invoke((Object)const__46, (Object)const__47);
        Object object13 = ((IFn)const__15.getRawRoot()).invoke((Object)const__46, const__16.getRawRoot(), ((IFn)const__11.getRawRoot()).invoke((Object)const__49, (Object)const__21, (Object)const__50, (Object)const__23, (Object)const__46, (Object)const__24, (Object)const__52, (Object)const__30, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__31.getRawRoot()).invoke(const__32.get(), ((IFn)const__33.getRawRoot()).invoke((Object)const__53, ((IFn)const__16.getRawRoot()).invoke((Object)const__54, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__36, const__46})))), new kv_store$fn__10820()})));
        Object object14 = ((IFn)const__43.getRawRoot()).invoke(const__46.getRawRoot());
        AFn aFn2 = const__55;
        Object object15 = ((IFn)const__56.getRawRoot()).invoke(const__57, const__46.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__51, new kv_store$fn__10831()}));
        Object object16 = ((IFn)const__56.getRawRoot()).invoke(const__58, const__46.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__51, new kv_store$fn__10833()}));
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.kv-store");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.kv-store", (String)"*retry*");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"dynamic"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__8 = RT.classForName((String)"datomic.kv_store.KVStore");
        const__9 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__10 = RT.var((String)"datomic.kv-store", (String)"KVStore");
        const__11 = RT.var((String)"clojure.core", (String)"assoc");
        const__12 = RT.keyword(null, (String)"doc");
        const__13 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__14 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-map"))))})), ((IObj)Symbol.intern(null, (String)"get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?"))))})), ((IObj)Symbol.intern(null, (String)"delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?"))))})), ((IObj)Symbol.intern(null, (String)"close")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__15 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__16 = RT.var((String)"clojure.core", (String)"merge");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.kv_store.KVStore"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.kv_store.KVStore")});
        const__21 = RT.keyword(null, (String)"sigs");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"put"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-map"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-map")))), RT.keyword(null, (String)"doc"), "(put {:id key :v buf :ensure check-map :other-keys ...}) -> :ok or nil\n          optional :ensure {:akey aval ...}\n          special treatment of {:id nil} == exists false"}), RT.keyword(null, (String)"get"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"doc"), "returns {:id key :v buf :other keys} or nil"}), RT.keyword(null, (String)"delete"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"doc"), "returns :ok"}), RT.keyword(null, (String)"close"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"close")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Closes resources opened for KVStore"})});
        const__23 = RT.keyword(null, (String)"var");
        const__24 = RT.keyword(null, (String)"method-map");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"get"), RT.keyword(null, (String)"get"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"close"), RT.keyword(null, (String)"close"), RT.keyword(null, (String)"put"), RT.keyword(null, (String)"put")});
        const__30 = RT.keyword(null, (String)"method-builders");
        const__31 = RT.var((String)"clojure.core", (String)"intern");
        const__32 = RT.var((String)"clojure.core", (String)"*ns*");
        const__33 = RT.var((String)"clojure.core", (String)"with-meta");
        const__34 = (AFn)((IObj)Symbol.intern(null, (String)"delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?"))))}));
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"doc"), "returns :ok"});
        const__36 = RT.keyword(null, (String)"protocol");
        const__37 = (AFn)((IObj)Symbol.intern(null, (String)"get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?"))))}));
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"consistent?")))), RT.keyword(null, (String)"doc"), "returns {:id key :v buf :other keys} or nil"});
        const__39 = (AFn)((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-map"))))}));
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-map"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"val-map")))), RT.keyword(null, (String)"doc"), "(put {:id key :v buf :ensure check-map :other-keys ...}) -> :ok or nil\n          optional :ensure {:akey aval ...}\n          special treatment of {:id nil} == exists false"});
        const__41 = (AFn)((IObj)Symbol.intern(null, (String)"close")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"close")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Closes resources opened for KVStore"});
        const__43 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__44 = (AFn)Symbol.intern(null, (String)"KVStore");
        const__45 = RT.classForName((String)"datomic.kv_store.Retryable");
        const__46 = RT.var((String)"datomic.kv-store", (String)"Retryable");
        const__47 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"retryable?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.kv_store.Retryable"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.kv_store.Retryable")});
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"retryable?"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"retryable?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), null})});
        const__51 = RT.keyword(null, (String)"retryable?");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"retryable?"), RT.keyword(null, (String)"retryable?")});
        const__53 = (AFn)((IObj)Symbol.intern(null, (String)"retryable?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"retryable?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), null});
        const__55 = (AFn)Symbol.intern(null, (String)"Retryable");
        const__56 = RT.var((String)"clojure.core", (String)"extend");
        const__57 = RT.classForName((String)"java.lang.Throwable");
        const__58 = RT.classForName((String)"java.lang.InterruptedException");
    }

    static {
        kv_store__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.kv_store__init").getClassLoader());
        try {
            kv_store__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


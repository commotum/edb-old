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
import datomic.simple_kv$fn__16675;
import datomic.simple_kv$fn__16683;
import datomic.simple_kv$fn__16686;
import datomic.simple_kv$fn__16701;
import datomic.simple_kv$fn__16714;
import datomic.simple_kv$get_with_retry;
import datomic.simple_kv$loading__6434__auto____16673;
import datomic.simple_kv$pack;
import datomic.simple_kv$unpack;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class simple_kv__init {
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
    public static final AFn const__23;
    public static final Keyword const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final Var const__27;
    public static final AFn const__28;
    public static final AFn const__29;
    public static final Keyword const__30;
    public static final AFn const__31;
    public static final AFn const__32;
    public static final AFn const__33;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__36;
    public static final Var const__37;
    public static final AFn const__41;
    public static final Object const__42;
    public static final Var const__43;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__52;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new simple_kv$loading__6434__auto____16673()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new simple_kv$fn__16675())));
            v2 = null;
        }
        Object object3 = ((IFn)new simple_kv$fn__16683()).invoke();
        Object object4 = const__3;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, const__6.getRawRoot(), (Object)const__7, null);
        Object object6 = ((IFn)const__8).invoke((Object)const__5, (Object)const__9);
        Object object7 = ((IFn)const__10.getRawRoot()).invoke((Object)const__5, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__5, (Object)const__19, (Object)const__23, (Object)const__24, (Object)RT.map((Object[])new Object[]{((IFn)const__25.getRawRoot()).invoke(const__26.get(), ((IFn)const__27.getRawRoot()).invoke((Object)const__28, ((IFn)const__11.getRawRoot()).invoke((Object)const__29, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__30, const__5})))), new simple_kv$fn__16686(), ((IFn)const__25.getRawRoot()).invoke(const__26.get(), ((IFn)const__27.getRawRoot()).invoke((Object)const__31, ((IFn)const__11.getRawRoot()).invoke((Object)const__32, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__30, const__5})))), new simple_kv$fn__16701(), ((IFn)const__25.getRawRoot()).invoke(const__26.get(), ((IFn)const__27.getRawRoot()).invoke((Object)const__33, ((IFn)const__11.getRawRoot()).invoke((Object)const__34, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__30, const__5})))), new simple_kv$fn__16714()})));
        Object object8 = ((IFn)const__35.getRawRoot()).invoke(const__5.getRawRoot());
        AFn aFn = const__36;
        Var var = const__37;
        var.setMeta((IPersistentMap)const__41);
        Var var2 = var;
        var.bindRoot(const__42);
        Var var3 = const__43;
        var3.setMeta((IPersistentMap)const__46);
        Var var4 = var3;
        var3.bindRoot((Object)new simple_kv$pack());
        Var var5 = const__47;
        var5.setMeta((IPersistentMap)const__49);
        Var var6 = var5;
        var5.bindRoot((Object)new simple_kv$unpack());
        Var var7 = const__50;
        var7.setMeta((IPersistentMap)const__52);
        Var var8 = var7;
        var7.bindRoot((Object)new simple_kv$get_with_retry());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.simple-kv");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.classForName((String)"datomic.simple_kv.KV");
        const__4 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__5 = RT.var((String)"datomic.simple-kv", (String)"KV");
        const__6 = RT.var((String)"clojure.core", (String)"assoc");
        const__7 = RT.keyword(null, (String)"doc");
        const__8 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__9 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"val"))))})), ((IObj)Symbol.intern(null, (String)"get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"))))})), ((IObj)Symbol.intern(null, (String)"delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"))))}))));
        const__10 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__11 = RT.var((String)"clojure.core", (String)"merge");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.simple_kv.KV"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.simple_kv.KV")});
        const__16 = RT.keyword(null, (String)"sigs");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"put"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"val"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"val")))), RT.keyword(null, (String)"doc"), "returns :ok or nil"}), RT.keyword(null, (String)"get"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key")))), RT.keyword(null, (String)"doc"), "returns ByteBuffer val or nil"}), RT.keyword(null, (String)"delete"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key")))), RT.keyword(null, (String)"doc"), "returns :ok"})});
        const__18 = RT.keyword(null, (String)"var");
        const__19 = RT.keyword(null, (String)"method-map");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"get"), RT.keyword(null, (String)"get"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"delete"), RT.keyword(null, (String)"put"), RT.keyword(null, (String)"put")});
        const__24 = RT.keyword(null, (String)"method-builders");
        const__25 = RT.var((String)"clojure.core", (String)"intern");
        const__26 = RT.var((String)"clojure.core", (String)"*ns*");
        const__27 = RT.var((String)"clojure.core", (String)"with-meta");
        const__28 = (AFn)((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"val"))))}));
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"val"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"), (Object)Symbol.intern(null, (String)"val")))), RT.keyword(null, (String)"doc"), "returns :ok or nil"});
        const__30 = RT.keyword(null, (String)"protocol");
        const__31 = (AFn)((IObj)Symbol.intern(null, (String)"delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"))))}));
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"delete")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key")))), RT.keyword(null, (String)"doc"), "returns :ok"});
        const__33 = (AFn)((IObj)Symbol.intern(null, (String)"get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"))))}));
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"key")))), RT.keyword(null, (String)"doc"), "returns ByteBuffer val or nil"});
        const__35 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__36 = (AFn)Symbol.intern(null, (String)"KV");
        const__37 = RT.var((String)"datomic.simple-kv", (String)"map-magic");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__42 = 568780356367818079L;
        const__43 = RT.var((String)"datomic.simple-kv", (String)"pack");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)((IObj)Symbol.intern(null, (String)"v")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ByteBuffer")}))))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.simple-kv", (String)"unpack");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"k"), (Object)((IObj)Symbol.intern(null, (String)"v")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ByteBuffer")}))))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.simple-kv", (String)"get-with-retry");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"skv"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        simple_kv__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.simple_kv__init").getClassLoader());
        try {
            simple_kv__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


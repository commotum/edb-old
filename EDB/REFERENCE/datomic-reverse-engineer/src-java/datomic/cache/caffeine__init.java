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
package datomic.cache;

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
import datomic.cache.caffeine$adapt_caffeine_cache;
import datomic.cache.caffeine$create_computing;
import datomic.cache.caffeine$create_limited;
import datomic.cache.caffeine$create_response_map;
import datomic.cache.caffeine$create_scaled_weight_limited;
import datomic.cache.caffeine$create_soft_limited;
import datomic.cache.caffeine$create_weight_limited;
import datomic.cache.caffeine$create_write_limited;
import datomic.cache.caffeine$fn__573;
import datomic.cache.caffeine$fn__575;
import datomic.cache.caffeine$fn__577;
import datomic.cache.caffeine$fn__579;
import datomic.cache.caffeine$fn__581;
import datomic.cache.caffeine$fn__585;
import datomic.cache.caffeine$fn__588;
import datomic.cache.caffeine$fn__601;
import datomic.cache.caffeine$fn__603;
import datomic.cache.caffeine$fn__605;
import datomic.cache.caffeine$loading__6434__auto____281;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class caffeine__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Object const__5;
    public static final Var const__6;
    public static final Keyword const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Var const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;
    public static final Var const__13;
    public static final AFn const__18;
    public static final Object const__19;
    public static final Object const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Keyword const__24;
    public static final Var const__25;
    public static final ISeq const__26;
    public static final Var const__27;
    public static final Var const__28;
    public static final AFn const__32;
    public static final Keyword const__33;
    public static final AFn const__34;
    public static final Keyword const__35;
    public static final Keyword const__36;
    public static final Keyword const__37;
    public static final AFn const__38;
    public static final Keyword const__39;
    public static final Var const__40;
    public static final Var const__41;
    public static final Var const__42;
    public static final AFn const__43;
    public static final AFn const__44;
    public static final Keyword const__45;
    public static final Var const__46;
    public static final AFn const__47;
    public static final Object const__48;
    public static final Var const__49;
    public static final AFn const__52;
    public static final Var const__53;
    public static final AFn const__55;
    public static final Var const__56;
    public static final AFn const__58;
    public static final Var const__59;
    public static final AFn const__61;
    public static final Var const__62;
    public static final AFn const__64;
    public static final Var const__65;
    public static final AFn const__67;
    public static final Var const__68;
    public static final AFn const__70;
    public static final Var const__71;
    public static final AFn const__73;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new caffeine$loading__6434__auto____281()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new caffeine$fn__573())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)const__4.getRawRoot()).invoke(const__5, const__6.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__7, new caffeine$fn__575()}));
        Object object5 = ((IFn)const__4.getRawRoot()).invoke(const__5, const__8.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__9, new caffeine$fn__577()}));
        Object object6 = ((IFn)const__4.getRawRoot()).invoke(const__5, const__10.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__11, new caffeine$fn__579(), const__12, new caffeine$fn__581()}));
        Var var = const__13;
        var.setMeta((IPersistentMap)const__18);
        Var var2 = var;
        var.bindRoot(const__19);
        Object object7 = ((IFn)new caffeine$fn__585()).invoke();
        Object object8 = const__20;
        Object object9 = ((IFn)const__21.getRawRoot()).invoke((Object)const__22, const__23.getRawRoot(), (Object)const__24, null);
        Object object10 = ((IFn)const__25).invoke((Object)const__22, (Object)const__26);
        Object object11 = ((IFn)const__27.getRawRoot()).invoke((Object)const__22, const__28.getRawRoot(), ((IFn)const__23.getRawRoot()).invoke((Object)const__32, (Object)const__33, (Object)const__34, (Object)const__35, (Object)const__22, (Object)const__36, (Object)const__38, (Object)const__39, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__40.getRawRoot()).invoke(const__41.get(), ((IFn)const__42.getRawRoot()).invoke((Object)const__43, ((IFn)const__28.getRawRoot()).invoke((Object)const__44, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__45, const__22})))), new caffeine$fn__588()})));
        Object object12 = ((IFn)const__46.getRawRoot()).invoke(const__22.getRawRoot());
        AFn aFn = const__47;
        Object object13 = ((IFn)const__4.getRawRoot()).invoke(const__5, const__22.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__37, new caffeine$fn__601()}));
        Object object14 = ((IFn)const__4.getRawRoot()).invoke(const__48, const__22.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__37, new caffeine$fn__603()}));
        Object object15 = ((IFn)new caffeine$fn__605()).invoke();
        Var var3 = const__49;
        var3.setMeta((IPersistentMap)const__52);
        Var var4 = var3;
        var3.bindRoot((Object)new caffeine$adapt_caffeine_cache());
        Var var5 = const__53;
        var5.setMeta((IPersistentMap)const__55);
        Var var6 = var5;
        var5.bindRoot((Object)new caffeine$create_write_limited());
        Var var7 = const__56;
        var7.setMeta((IPersistentMap)const__58);
        Var var8 = var7;
        var7.bindRoot((Object)new caffeine$create_limited());
        Var var9 = const__59;
        var9.setMeta((IPersistentMap)const__61);
        Var var10 = var9;
        var9.bindRoot((Object)new caffeine$create_soft_limited());
        Var var11 = const__62;
        var11.setMeta((IPersistentMap)const__64);
        Var var12 = var11;
        var11.bindRoot((Object)new caffeine$create_weight_limited());
        Var var13 = const__65;
        var13.setMeta((IPersistentMap)const__67);
        Var var14 = var13;
        var13.bindRoot((Object)new caffeine$create_scaled_weight_limited());
        Var var15 = const__68;
        var15.setMeta((IPersistentMap)const__70);
        Var var16 = var15;
        var15.bindRoot((Object)new caffeine$create_computing());
        Var var17 = const__71;
        var17.setMeta((IPersistentMap)const__73);
        Var var18 = var17;
        var17.bindRoot((Object)new caffeine$create_response_map());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cache.caffeine");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"clojure.core", (String)"extend");
        const__5 = RT.classForName((String)"com.github.benmanes.caffeine.cache.Cache");
        const__6 = RT.var((String)"datomic.cache.impl", (String)"FastCount");
        const__7 = RT.keyword(null, (String)"fast-count");
        const__8 = RT.var((String)"datomic.cache.impl", (String)"CachePut");
        const__9 = RT.keyword(null, (String)"put");
        const__10 = RT.var((String)"datomic.cache.impl", (String)"CacheRemove");
        const__11 = RT.keyword(null, (String)"remove");
        const__12 = RT.keyword(null, (String)"clear");
        const__13 = RT.var((String)"datomic.cache.caffeine", (String)"CAFFEINE_ENTRY_OVERHEAD");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__19 = 82L;
        const__20 = RT.classForName((String)"datomic.cache.caffeine.CacheGet");
        const__21 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__22 = RT.var((String)"datomic.cache.caffeine", (String)"CacheGet");
        const__23 = RT.var((String)"clojure.core", (String)"assoc");
        const__24 = RT.keyword(null, (String)"doc");
        const__25 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__26 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"cache-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))}))));
        const__27 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__28 = RT.var((String)"clojure.core", (String)"merge");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.cache.caffeine.CacheGet"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.cache.caffeine.CacheGet")});
        const__33 = RT.keyword(null, (String)"sigs");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"cache-get"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"cache-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), null})});
        const__35 = RT.keyword(null, (String)"var");
        const__36 = RT.keyword(null, (String)"method-map");
        const__37 = RT.keyword(null, (String)"cache-get");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"cache-get"), RT.keyword(null, (String)"cache-get")});
        const__39 = RT.keyword(null, (String)"method-builders");
        const__40 = RT.var((String)"clojure.core", (String)"intern");
        const__41 = RT.var((String)"clojure.core", (String)"*ns*");
        const__42 = RT.var((String)"clojure.core", (String)"with-meta");
        const__43 = (AFn)((IObj)Symbol.intern(null, (String)"cache-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))}));
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"cache-get")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), null});
        const__45 = RT.keyword(null, (String)"protocol");
        const__46 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__47 = (AFn)Symbol.intern(null, (String)"CacheGet");
        const__48 = RT.classForName((String)"com.github.benmanes.caffeine.cache.LoadingCache");
        const__49 = RT.var((String)"datomic.cache.caffeine", (String)"adapt-caffeine-cache");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cache")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Cache")}))))), RT.keyword(null, (String)"column"), 1});
        const__53 = RT.var((String)"datomic.cache.caffeine", (String)"create-write-limited");
        const__55 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"num-entries")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"num-entries")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)((IObj)Symbol.intern(null, (String)"timeout-minutes")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))))), RT.keyword(null, (String)"column"), 1});
        const__56 = RT.var((String)"datomic.cache.caffeine", (String)"create-limited");
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"num-entries")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"num-entries")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)((IObj)Symbol.intern(null, (String)"timeout-minutes")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))))), RT.keyword(null, (String)"column"), 1});
        const__59 = RT.var((String)"datomic.cache.caffeine", (String)"create-soft-limited");
        const__61 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create(), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"num-entries")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"num-entries")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)((IObj)Symbol.intern(null, (String)"timeout-minutes")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))))), RT.keyword(null, (String)"column"), 1});
        const__62 = RT.var((String)"datomic.cache.caffeine", (String)"create-weight-limited");
        const__64 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"weight"), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__65 = RT.var((String)"datomic.cache.caffeine", (String)"create-scaled-weight-limited");
        const__67 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"weight"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"divisor")))), RT.keyword(null, (String)"column"), 1});
        const__68 = RT.var((String)"datomic.cache.caffeine", (String)"create-computing");
        const__70 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"max-size")))), RT.keyword(null, (String)"column"), 1});
        const__71 = RT.var((String)"datomic.cache.caffeine", (String)"create-response-map");
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"timeout-minutes")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        caffeine__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cache.caffeine__init").getClassLoader());
        try {
            caffeine__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
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
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.cache.impl$fn__285;
import datomic.cache.impl$fn__289;
import datomic.cache.impl$fn__292;
import datomic.cache.impl$fn__305;
import datomic.cache.impl$fn__308;
import datomic.cache.impl$fn__321;
import datomic.cache.impl$fn__324;
import datomic.cache.impl$fn__343;
import datomic.cache.impl$fn__346;
import datomic.cache.impl$fn__357;
import datomic.cache.impl$fn__370;
import datomic.cache.impl$fn__372;
import datomic.cache.impl$fn__374;
import datomic.cache.impl$fn__376;
import datomic.cache.impl$loading__6434__auto____283;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class impl__init {
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
    public static final Keyword const__20;
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
    public static final Keyword const__48;
    public static final AFn const__49;
    public static final AFn const__50;
    public static final AFn const__51;
    public static final AFn const__52;
    public static final Object const__53;
    public static final Var const__54;
    public static final ISeq const__55;
    public static final AFn const__57;
    public static final AFn const__58;
    public static final Keyword const__59;
    public static final Keyword const__60;
    public static final AFn const__61;
    public static final AFn const__62;
    public static final AFn const__63;
    public static final AFn const__64;
    public static final AFn const__65;
    public static final AFn const__66;
    public static final Var const__67;
    public static final Object const__68;
    public static final Object const__69;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new impl$loading__6434__auto____283()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new impl$fn__285())));
            v2 = null;
        }
        Object object3 = ((IFn)new impl$fn__289()).invoke();
        Object object4 = const__3;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, const__6.getRawRoot(), (Object)const__7, null);
        Object object6 = ((IFn)const__8).invoke((Object)const__5, (Object)const__9);
        Object object7 = ((IFn)const__10.getRawRoot()).invoke((Object)const__5, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__5, (Object)const__19, (Object)const__21, (Object)const__22, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__26, ((IFn)const__11.getRawRoot()).invoke((Object)const__27, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__5})))), new impl$fn__292()})));
        Object object8 = ((IFn)const__29.getRawRoot()).invoke(const__5.getRawRoot());
        AFn aFn = const__30;
        Object object9 = ((IFn)new impl$fn__305()).invoke();
        Object object10 = const__31;
        Object object11 = ((IFn)const__4.getRawRoot()).invoke((Object)const__32, const__6.getRawRoot(), (Object)const__7, null);
        Object object12 = ((IFn)const__8).invoke((Object)const__32, (Object)const__33);
        Object object13 = ((IFn)const__10.getRawRoot()).invoke((Object)const__32, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__35, (Object)const__16, (Object)const__36, (Object)const__18, (Object)const__32, (Object)const__19, (Object)const__38, (Object)const__22, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__39, ((IFn)const__11.getRawRoot()).invoke((Object)const__40, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__32})))), new impl$fn__308()})));
        Object object14 = ((IFn)const__29.getRawRoot()).invoke(const__32.getRawRoot());
        AFn aFn2 = const__41;
        Object object15 = ((IFn)new impl$fn__321()).invoke();
        Object object16 = const__42;
        Object object17 = ((IFn)const__4.getRawRoot()).invoke((Object)const__43, const__6.getRawRoot(), (Object)const__7, null);
        Object object18 = ((IFn)const__8).invoke((Object)const__43, (Object)const__44);
        Object object19 = ((IFn)const__10.getRawRoot()).invoke((Object)const__43, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__46, (Object)const__16, (Object)const__47, (Object)const__18, (Object)const__43, (Object)const__19, (Object)const__49, (Object)const__22, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__50, ((IFn)const__11.getRawRoot()).invoke((Object)const__51, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__43})))), new impl$fn__324()})));
        Object object20 = ((IFn)const__29.getRawRoot()).invoke(const__43.getRawRoot());
        AFn aFn3 = const__52;
        Object object21 = ((IFn)new impl$fn__343()).invoke();
        Object object22 = const__53;
        Object object23 = ((IFn)const__4.getRawRoot()).invoke((Object)const__54, const__6.getRawRoot(), (Object)const__7, null);
        Object object24 = ((IFn)const__8).invoke((Object)const__54, (Object)const__55);
        Object object25 = ((IFn)const__10.getRawRoot()).invoke((Object)const__54, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__57, (Object)const__16, (Object)const__58, (Object)const__18, (Object)const__54, (Object)const__19, (Object)const__61, (Object)const__22, (Object)RT.map((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__62, ((IFn)const__11.getRawRoot()).invoke((Object)const__63, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__54})))), new impl$fn__346(), ((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__64, ((IFn)const__11.getRawRoot()).invoke((Object)const__65, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__54})))), new impl$fn__357()})));
        Object object26 = ((IFn)const__29.getRawRoot()).invoke(const__54.getRawRoot());
        AFn aFn4 = const__66;
        Object object27 = ((IFn)const__67.getRawRoot()).invoke(const__68, const__5.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__20, new impl$fn__370()}));
        Object object28 = ((IFn)const__67.getRawRoot()).invoke(const__69, const__43.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__48, new impl$fn__372()}));
        Object object29 = ((IFn)const__67.getRawRoot()).invoke(const__69, const__54.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__59, new impl$fn__374(), const__60, new impl$fn__376()}));
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cache.impl");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.classForName((String)"datomic.cache.impl.FastCount");
        const__4 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__5 = RT.var((String)"datomic.cache.impl", (String)"FastCount");
        const__6 = RT.var((String)"clojure.core", (String)"assoc");
        const__7 = RT.keyword(null, (String)"doc");
        const__8 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__9 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"fast-count")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__10 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__11 = RT.var((String)"clojure.core", (String)"merge");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.cache.impl.FastCount"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.cache.impl.FastCount")});
        const__16 = RT.keyword(null, (String)"sigs");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"fast-count"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"fast-count")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Count a collection, preferring speed over exact accuracy."})});
        const__18 = RT.keyword(null, (String)"var");
        const__19 = RT.keyword(null, (String)"method-map");
        const__20 = RT.keyword(null, (String)"fast-count");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"fast-count"), RT.keyword(null, (String)"fast-count")});
        const__22 = RT.keyword(null, (String)"method-builders");
        const__23 = RT.var((String)"clojure.core", (String)"intern");
        const__24 = RT.var((String)"clojure.core", (String)"*ns*");
        const__25 = RT.var((String)"clojure.core", (String)"with-meta");
        const__26 = (AFn)((IObj)Symbol.intern(null, (String)"fast-count")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"fast-count")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Count a collection, preferring speed over exact accuracy."});
        const__28 = RT.keyword(null, (String)"protocol");
        const__29 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__30 = (AFn)Symbol.intern(null, (String)"FastCount");
        const__31 = RT.classForName((String)"datomic.cache.impl.CacheKeys");
        const__32 = RT.var((String)"datomic.cache.impl", (String)"CacheKeys");
        const__33 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"cache-keys")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.cache.impl.CacheKeys"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.cache.impl.CacheKeys")});
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"cache-keys"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"cache-keys")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns cache keys"})});
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"cache-keys"), RT.keyword(null, (String)"cache-keys")});
        const__39 = (AFn)((IObj)Symbol.intern(null, (String)"cache-keys")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"cache-keys")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns cache keys"});
        const__41 = (AFn)Symbol.intern(null, (String)"CacheKeys");
        const__42 = RT.classForName((String)"datomic.cache.impl.CachePut");
        const__43 = RT.var((String)"datomic.cache.impl", (String)"CachePut");
        const__44 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"))))}))));
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.cache.impl.CachePut"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.cache.impl.CachePut")});
        const__47 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"put"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"doc"), "Put item into the cache. No useful return value."})});
        const__48 = RT.keyword(null, (String)"put");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"put"), RT.keyword(null, (String)"put")});
        const__50 = (AFn)((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"))))}));
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"put")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"doc"), "Put item into the cache. No useful return value."});
        const__52 = (AFn)Symbol.intern(null, (String)"CachePut");
        const__53 = RT.classForName((String)"datomic.cache.impl.CacheRemove");
        const__54 = RT.var((String)"datomic.cache.impl", (String)"CacheRemove");
        const__55 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"remove")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"))))})), ((IObj)Symbol.intern(null, (String)"clear")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"))))}))));
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.cache.impl.CacheRemove"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.cache.impl.CacheRemove")});
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"remove"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"remove")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), "Remove item from cache, returning it."}), RT.keyword(null, (String)"clear"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"clear")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c")))), RT.keyword(null, (String)"doc"), "Remove all items from cache"})});
        const__59 = RT.keyword(null, (String)"remove");
        const__60 = RT.keyword(null, (String)"clear");
        const__61 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"remove"), RT.keyword(null, (String)"remove"), RT.keyword(null, (String)"clear"), RT.keyword(null, (String)"clear")});
        const__62 = (AFn)((IObj)Symbol.intern(null, (String)"clear")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"))))}));
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"clear")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c")))), RT.keyword(null, (String)"doc"), "Remove all items from cache"});
        const__64 = (AFn)((IObj)Symbol.intern(null, (String)"remove")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"))))}));
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"remove")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"doc"), "Remove item from cache, returning it."});
        const__66 = (AFn)Symbol.intern(null, (String)"CacheRemove");
        const__67 = RT.var((String)"clojure.core", (String)"extend");
        const__68 = RT.classForName((String)"java.lang.Object");
        const__69 = RT.classForName((String)"java.util.concurrent.ConcurrentMap");
    }

    static {
        impl__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cache.impl__init").getClassLoader());
        try {
            impl__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


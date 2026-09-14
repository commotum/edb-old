/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
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
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.domain$create_object_cache;
import datomic.domain$deserialize;
import datomic.domain$deserializing_repairing_lookup;
import datomic.domain$fn__16669;
import datomic.domain$fn__16767;
import datomic.domain$fn__16769;
import datomic.domain$fn__16771;
import datomic.domain$fn__16773;
import datomic.domain$fn__16775;
import datomic.domain$fn__16781;
import datomic.domain$fn__16786;
import datomic.domain$fn__16788;
import datomic.domain$fn__16790;
import datomic.domain$fn__16803;
import datomic.domain$loading__6434__auto____14175;
import datomic.domain$lookup_with_object_cache;
import datomic.domain$peer_object_lookup;
import datomic.domain$preload_extension_resolver_BANG_;
import datomic.domain$system_cache;
import datomic.domain$system_cache_olookup;
import datomic.domain$uncached_lookup_factory;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class domain__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__5;
    public static final Var const__6;
    public static final Object const__7;
    public static final Var const__8;
    public static final Keyword const__9;
    public static final Object const__10;
    public static final Object const__11;
    public static final Object const__12;
    public static final Object const__13;
    public static final Var const__14;
    public static final AFn const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;
    public static final Var const__30;
    public static final Object const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__37;
    public static final Var const__38;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final Var const__48;
    public static final AFn const__50;
    public static final Var const__51;
    public static final AFn const__53;
    public static final Var const__54;
    public static final AFn const__56;
    public static final Var const__57;
    public static final AFn const__59;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new domain$loading__6434__auto____14175()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new domain$fn__16669())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5);
        Object object5 = ((IFn)const__6.getRawRoot()).invoke(const__7, const__8.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__9, new domain$fn__16767()}));
        Object object6 = ((IFn)const__6.getRawRoot()).invoke(const__10, const__8.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__9, new domain$fn__16769()}));
        Object object7 = ((IFn)const__6.getRawRoot()).invoke(const__11, const__8.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__9, new domain$fn__16771()}));
        Object object8 = ((IFn)const__6.getRawRoot()).invoke(const__12, const__8.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__9, new domain$fn__16773()}));
        Object object9 = ((IFn)const__6.getRawRoot()).invoke(const__13, const__8.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__9, new domain$fn__16775()}));
        Var var = const__14;
        var.setMeta((IPersistentMap)const__17);
        Var var2 = var;
        var.bindRoot(((IFn)const__18.getRawRoot()).invoke(const__19.getRawRoot(), const__20.getRawRoot(), const__21.getRawRoot(), const__22.getRawRoot()));
        Var var3 = const__23;
        var3.setMeta((IPersistentMap)const__26);
        Var var4 = var3;
        var3.bindRoot((Object)new domain$uncached_lookup_factory());
        Var var5 = const__27;
        var5.setMeta((IPersistentMap)const__29);
        Var var6 = var5;
        var5.bindRoot((Object)new domain$create_object_cache());
        Object object10 = ((IFn)new domain$fn__16781()).invoke();
        MultiFn multiFn = ((MultiFn)const__30.getRawRoot()).addMethod(const__31, (IFn)new domain$fn__16786());
        MultiFn multiFn2 = ((MultiFn)const__32.getRawRoot()).addMethod(const__31, (IFn)new domain$fn__16788());
        Var var7 = const__33;
        var7.setMeta((IPersistentMap)const__34);
        Var var8 = var7;
        var7.bindRoot((Object)new Delay((IFn)new domain$fn__16790()));
        Var var9 = const__35;
        var9.setMeta((IPersistentMap)const__37);
        Var var10 = var9;
        var9.bindRoot((Object)new domain$system_cache());
        Var var11 = const__38;
        var11.setMeta((IPersistentMap)const__40);
        Var var12 = var11;
        var11.bindRoot((Object)new domain$peer_object_lookup());
        Var var13 = const__41;
        var13.setMeta((IPersistentMap)const__43);
        Var var14 = var13;
        var13.bindRoot((Object)new domain$preload_extension_resolver_BANG_());
        Var var15 = const__44;
        var15.setMeta((IPersistentMap)const__46);
        Var var16 = var15;
        var15.bindRoot(((IFn)const__47.getRawRoot()).invoke(const__14.getRawRoot()));
        Var var17 = const__48;
        var17.setMeta((IPersistentMap)const__50);
        Var var18 = var17;
        var17.bindRoot((Object)new domain$deserialize());
        Object object11 = ((IFn)new domain$fn__16803()).invoke();
        Var var19 = const__51;
        var19.setMeta((IPersistentMap)const__53);
        Var var20 = var19;
        var19.bindRoot((Object)new domain$deserializing_repairing_lookup());
        Var var21 = const__54;
        var21.setMeta((IPersistentMap)const__56);
        Var var22 = var21;
        var21.bindRoot((Object)new domain$lookup_with_object_cache());
        Var var23 = const__57;
        var23.setMeta((IPersistentMap)const__59);
        Var var24 = var23;
        var23.bindRoot((Object)new domain$system_cache_olookup());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.domain");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.require", (String)"maybe-require");
        const__5 = (AFn)Symbol.intern(null, (String)"datomic.coordination-ext");
        const__6 = RT.var((String)"clojure.core", (String)"extend");
        const__7 = RT.classForName((String)"datomic.index.TransposedData");
        const__8 = RT.var((String)"datomic.memory-size", (String)"MemorySize");
        const__9 = RT.keyword(null, (String)"memory-size");
        const__10 = RT.classForName((String)"datomic.index.RootNode");
        const__11 = RT.classForName((String)"datomic.index.DirNode");
        const__12 = RT.classForName((String)"datomic.btset.BTSet");
        const__13 = RT.classForName((String)"datomic.db.Datum");
        const__14 = RT.var((String)"datomic.domain", (String)"common-read-handlers");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"clojure.core", (String)"merge");
        const__19 = RT.var((String)"datomic.log", (String)"read-handlers");
        const__20 = RT.var((String)"datomic.clusterfs", (String)"read-handlers");
        const__21 = RT.var((String)"datomic.fulltext", (String)"read-handlers");
        const__22 = RT.var((String)"datomic.garbage.fressian", (String)"read-handlers");
        const__23 = RT.var((String)"datomic.domain", (String)"uncached-lookup-factory");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.domain", (String)"create-object-cache");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create(), Tuple.create((Object)Symbol.intern(null, (String)"cache-bytes")))), RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"clojure.core", (String)"print-method");
        const__31 = RT.classForName((String)"datomic.domain.ValcachePoller");
        const__32 = RT.var((String)"clojure.core", (String)"print-dup");
        const__33 = RT.var((String)"datomic.domain", (String)"cache-delay");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.domain", (String)"system-cache");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__38 = RT.var((String)"datomic.domain", (String)"peer-object-lookup");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"val-lookup"), (Object)Symbol.intern(null, (String)"read-lookup")), Tuple.create((Object)Symbol.intern(null, (String)"val-lookup"), (Object)Symbol.intern(null, (String)"read-lookup"), (Object)Symbol.intern(null, (String)"object-cache")))), RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.domain", (String)"preload-extension-resolver!");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.domain", (String)"defressian");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.fressian", (String)"val->obj");
        const__48 = RT.var((String)"datomic.domain", (String)"deserialize");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"datomic.domain", (String)"deserializing-repairing-lookup");
        const__53 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
        const__54 = RT.var((String)"datomic.domain", (String)"lookup-with-object-cache");
        const__56 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"lookup")), Tuple.create((Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"object-cache")))), RT.keyword(null, (String)"column"), 1});
        const__57 = RT.var((String)"datomic.domain", (String)"system-cache-olookup");
        const__59 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cluster")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        domain__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.domain__init").getClassLoader());
        try {
            domain__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


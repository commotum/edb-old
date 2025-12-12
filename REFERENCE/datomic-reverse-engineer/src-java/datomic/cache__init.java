/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
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
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.cache$double_lookup;
import datomic.cache$fn__9372;
import datomic.cache$fn__9374;
import datomic.cache$fn__9379;
import datomic.cache$fn__9382;
import datomic.cache$fn__GT_lookup;
import datomic.cache$get_from_cache;
import datomic.cache$get_uncached;
import datomic.cache$getx_uncached;
import datomic.cache$loading__6434__auto____279;
import datomic.cache$lookup_cache;
import datomic.cache$lookup_transformer;
import datomic.cache$lookup_with_inflight_cache;
import datomic.cache$read_ahead;
import datomic.cache$repairing_cache_stack;
import datomic.cache$report_val_fn_fail;
import datomic.cache$safe_lookup_transformer;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class cache__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final AFn const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final AFn const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final AFn const__19;
    public static final Var const__20;
    public static final Var const__21;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__27;
    public static final Var const__28;
    public static final AFn const__30;
    public static final Var const__31;
    public static final AFn const__33;
    public static final Var const__34;
    public static final AFn const__36;
    public static final Var const__37;
    public static final AFn const__39;
    public static final Var const__40;
    public static final AFn const__42;
    public static final Var const__43;
    public static final AFn const__45;
    public static final Var const__46;
    public static final AFn const__48;
    public static final Var const__49;
    public static final AFn const__51;
    public static final Var const__52;
    public static final AFn const__54;
    public static final Var const__55;
    public static final AFn const__57;
    public static final Var const__58;
    public static final AFn const__60;
    public static final Var const__61;
    public static final AFn const__62;
    public static final Var const__63;
    public static final Var const__64;
    public static final AFn const__65;
    public static final Var const__66;
    public static final Var const__67;
    public static final AFn const__68;
    public static final Var const__69;
    public static final Var const__70;
    public static final AFn const__71;
    public static final Var const__72;
    public static final Var const__73;
    public static final AFn const__74;
    public static final Var const__75;
    public static final Var const__76;
    public static final AFn const__77;
    public static final Var const__78;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new cache$loading__6434__auto____279()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new cache$fn__9372())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot((Object)const__8);
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__10);
        Var var4 = var3;
        var3.bindRoot((Object)const__11);
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__13);
        Var var6 = var5;
        var5.bindRoot((Object)const__14);
        Var var7 = const__15;
        var7.setMeta((IPersistentMap)const__16);
        Var var8 = var7;
        var7.bindRoot((Object)const__17);
        Var var9 = const__18;
        var9.setMeta((IPersistentMap)const__19);
        Var var10 = var9;
        var9.bindRoot((Object)const__20);
        Object object4 = ((IFn)new cache$fn__9374()).invoke();
        Object object5 = ((IFn)new cache$fn__9379()).invoke();
        Var var11 = const__21;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new cache$get_from_cache());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new Delay((IFn)new cache$fn__9382()));
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot((Object)new cache$read_ahead());
        Var var17 = const__31;
        var17.setMeta((IPersistentMap)const__33);
        Var var18 = var17;
        var17.bindRoot((Object)new cache$get_uncached());
        Var var19 = const__34;
        var19.setMeta((IPersistentMap)const__36);
        Var var20 = var19;
        var19.bindRoot((Object)new cache$getx_uncached());
        Var var21 = const__37;
        var21.setMeta((IPersistentMap)const__39);
        Var var22 = var21;
        var21.bindRoot((Object)new cache$report_val_fn_fail());
        Var var23 = const__40;
        var23.setMeta((IPersistentMap)const__42);
        Var var24 = var23;
        var23.bindRoot((Object)new cache$lookup_transformer());
        Var var25 = const__43;
        var25.setMeta((IPersistentMap)const__45);
        Var var26 = var25;
        var25.bindRoot((Object)new cache$safe_lookup_transformer());
        Var var27 = const__46;
        var27.setMeta((IPersistentMap)const__48);
        Var var28 = var27;
        var27.bindRoot((Object)new cache$lookup_cache());
        Var var29 = const__49;
        var29.setMeta((IPersistentMap)const__51);
        Var var30 = var29;
        var29.bindRoot((Object)new cache$lookup_with_inflight_cache());
        Var var31 = const__52;
        var31.setMeta((IPersistentMap)const__54);
        Var var32 = var31;
        var31.bindRoot((Object)new cache$fn__GT_lookup());
        Var var33 = const__55;
        var33.setMeta((IPersistentMap)const__57);
        Var var34 = var33;
        var33.bindRoot((Object)new cache$double_lookup());
        Var var35 = const__58;
        var35.setMeta((IPersistentMap)const__60);
        Var var36 = var35;
        var35.bindRoot((Object)new cache$repairing_cache_stack());
        Var var37 = const__61;
        var37.setMeta((IPersistentMap)const__62);
        Var var38 = var37;
        var37.bindRoot((Object)const__63);
        Var var39 = const__64;
        var39.setMeta((IPersistentMap)const__65);
        Var var40 = var39;
        var39.bindRoot((Object)const__66);
        Var var41 = const__67;
        var41.setMeta((IPersistentMap)const__68);
        Var var42 = var41;
        var41.bindRoot((Object)const__69);
        Var var43 = const__70;
        var43.setMeta((IPersistentMap)const__71);
        Var var44 = var43;
        var43.bindRoot((Object)const__72);
        Var var45 = const__73;
        var45.setMeta((IPersistentMap)const__74);
        Var var46 = var45;
        var45.bindRoot((Object)const__75);
        Var var47 = const__76;
        var47.setMeta((IPersistentMap)const__77);
        Var var48 = var47;
        var47.bindRoot((Object)const__78);
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cache");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.cache", (String)"fast-count");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__8 = RT.var((String)"datomic.cache.impl", (String)"fast-count");
        const__9 = RT.var((String)"datomic.cache", (String)"cache-keys");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.cache.impl", (String)"cache-keys");
        const__12 = RT.var((String)"datomic.cache", (String)"put");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__14 = RT.var((String)"datomic.cache.impl", (String)"put");
        const__15 = RT.var((String)"datomic.cache", (String)"remove");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.cache.impl", (String)"remove");
        const__18 = RT.var((String)"datomic.cache", (String)"clear");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.cache.impl", (String)"clear");
        const__21 = RT.var((String)"datomic.cache", (String)"get-from-cache");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"nf")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.cache", (String)"read-ahead-pool-prop");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.cache", (String)"read-ahead");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.cache", (String)"get-uncached");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"nf")))), RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"datomic.cache", (String)"getx-uncached");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__37 = RT.var((String)"datomic.cache", (String)"report-val-fn-fail");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"t"), (Object)Symbol.intern(null, (String)"raw"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__40 = RT.var((String)"datomic.cache", (String)"lookup-transformer");
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"key-fn"), (Object)Symbol.intern(null, (String)"val-fn")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"key-fn"), Symbol.intern(null, (String)"identity"), Symbol.intern(null, (String)"val-fn"), Symbol.intern(null, (String)"identity")})})))), RT.keyword(null, (String)"column"), 1});
        const__43 = RT.var((String)"datomic.cache", (String)"safe-lookup-transformer");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"key-fn"), (Object)Symbol.intern(null, (String)"val-fn")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"key-fn"), Symbol.intern(null, (String)"identity"), Symbol.intern(null, (String)"val-fn"), Symbol.intern(null, (String)"identity")})})))), RT.keyword(null, (String)"column"), 1});
        const__46 = RT.var((String)"datomic.cache", (String)"lookup-cache");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"cache")), Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"cache"), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"datomic.cache", (String)"lookup-with-inflight-cache");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.cache", (String)"fn->lookup");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.cache", (String)"double-lookup");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m1"), (Object)Symbol.intern(null, (String)"m2")))), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.cache", (String)"repairing-cache-stack");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cache-1")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"AutoCloseable")})), (Object)((IObj)Symbol.intern(null, (String)"cache-2")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"AutoCloseable")})), (Object)Symbol.intern(null, (String)"close-cache-1?"), (Object)Symbol.intern(null, (String)"close-cache-2?"), (Object)Symbol.intern(null, (String)"on-repair")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"close-cache-1?"), Boolean.TRUE, Symbol.intern(null, (String)"close-cache-2?"), Boolean.TRUE})})))), RT.keyword(null, (String)"column"), 1});
        const__61 = RT.var((String)"datomic.cache", (String)"create-write-limited");
        const__62 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__63 = RT.var((String)"datomic.cache.caffeine", (String)"create-write-limited");
        const__64 = RT.var((String)"datomic.cache", (String)"create-limited");
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__66 = RT.var((String)"datomic.cache.caffeine", (String)"create-limited");
        const__67 = RT.var((String)"datomic.cache", (String)"create-soft-limited");
        const__68 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__69 = RT.var((String)"datomic.cache.caffeine", (String)"create-soft-limited");
        const__70 = RT.var((String)"datomic.cache", (String)"create-scaled-weight-limited");
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__72 = RT.var((String)"datomic.cache.caffeine", (String)"create-scaled-weight-limited");
        const__73 = RT.var((String)"datomic.cache", (String)"create-computing");
        const__74 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__75 = RT.var((String)"datomic.cache.caffeine", (String)"create-computing");
        const__76 = RT.var((String)"datomic.cache", (String)"create-response-map");
        const__77 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__78 = RT.var((String)"datomic.cache.caffeine", (String)"create-response-map");
    }

    static {
        cache__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cache__init").getClassLoader());
        try {
            cache__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


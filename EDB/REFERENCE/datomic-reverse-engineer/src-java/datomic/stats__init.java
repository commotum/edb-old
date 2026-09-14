/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.PersistentHashSet
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
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.stats$aevt;
import datomic.stats$attr_stats_from_splits;
import datomic.stats$avet;
import datomic.stats$datom_count;
import datomic.stats$datom_counts;
import datomic.stats$db_attr_splits;
import datomic.stats$db_attr_stats;
import datomic.stats$db_stats;
import datomic.stats$eavt;
import datomic.stats$fn__17831;
import datomic.stats$fulltext;
import datomic.stats$index_attr_splits;
import datomic.stats$index_attr_stats;
import datomic.stats$index_datom_count;
import datomic.stats$index_metrics;
import datomic.stats$index_summary;
import datomic.stats$key_summary;
import datomic.stats$loading__6434__auto____17829;
import datomic.stats$merge_data;
import datomic.stats$merge_splits;
import datomic.stats$raet;
import datomic.stats$segment_count;
import datomic.stats$sizes;
import datomic.stats$sizes__GT_metrics;
import datomic.stats$sparse_v_count;
import datomic.stats$total;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class stats__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__10;
    public static final Var const__11;
    public static final AFn const__13;
    public static final Var const__14;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__19;
    public static final Var const__20;
    public static final AFn const__22;
    public static final Var const__23;
    public static final AFn const__25;
    public static final Var const__26;
    public static final AFn const__28;
    public static final Var const__29;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__37;
    public static final Var const__38;
    public static final AFn const__39;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__45;
    public static final AFn const__48;
    public static final Var const__49;
    public static final AFn const__50;
    public static final Var const__51;
    public static final Var const__52;
    public static final AFn const__54;
    public static final Var const__55;
    public static final AFn const__57;
    public static final Var const__58;
    public static final AFn const__60;
    public static final Var const__61;
    public static final AFn const__63;
    public static final Var const__64;
    public static final AFn const__66;
    public static final Var const__67;
    public static final AFn const__69;
    public static final Var const__70;
    public static final AFn const__72;
    public static final Var const__73;
    public static final AFn const__75;
    public static final Var const__76;
    public static final AFn const__78;
    public static final Var const__79;
    public static final AFn const__81;
    public static final Var const__82;
    public static final AFn const__84;
    public static final Var const__85;
    public static final AFn const__87;
    public static final Var const__88;
    public static final AFn const__90;
    public static final Var const__91;
    public static final AFn const__93;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new stats$loading__6434__auto____17829()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new stats$fn__17831())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)new stats$sparse_v_count());
        Var var3 = const__11;
        var3.setMeta((IPersistentMap)const__13);
        Var var4 = var3;
        var3.bindRoot((Object)new stats$key_summary());
        Var var5 = const__14;
        var5.setMeta((IPersistentMap)const__16);
        Var var6 = var5;
        var5.bindRoot((Object)new stats$index_summary());
        Var var7 = const__17;
        var7.setMeta((IPersistentMap)const__19);
        Var var8 = var7;
        var7.bindRoot((Object)new stats$avet());
        Var var9 = const__20;
        var9.setMeta((IPersistentMap)const__22);
        Var var10 = var9;
        var9.bindRoot((Object)new stats$aevt());
        Var var11 = const__23;
        var11.setMeta((IPersistentMap)const__25);
        Var var12 = var11;
        var11.bindRoot((Object)new stats$eavt());
        Var var13 = const__26;
        var13.setMeta((IPersistentMap)const__28);
        Var var14 = var13;
        var13.bindRoot((Object)new stats$raet());
        Var var15 = const__29;
        var15.setMeta((IPersistentMap)const__31);
        Var var16 = var15;
        var15.bindRoot((Object)new stats$total());
        Var var17 = const__32;
        var17.setMeta((IPersistentMap)const__34);
        Var var18 = var17;
        var17.bindRoot((Object)new stats$datom_counts());
        Var var19 = const__35;
        var19.setMeta((IPersistentMap)const__37);
        Var var20 = var19;
        var19.bindRoot((Object)new stats$fulltext());
        Var var21 = const__38;
        var21.setMeta((IPersistentMap)const__39);
        Var var22 = var21;
        var21.bindRoot((Object)const__43);
        Var var23 = const__44;
        var23.setMeta((IPersistentMap)const__45);
        Var var24 = var23;
        var23.bindRoot((Object)const__48);
        Var var25 = const__49;
        var25.setMeta((IPersistentMap)const__50);
        Var var26 = var25;
        var25.bindRoot(((IFn)const__51.getRawRoot()).invoke(const__38.getRawRoot(), const__44.getRawRoot()));
        Var var27 = const__52;
        var27.setMeta((IPersistentMap)const__54);
        Var var28 = var27;
        var27.bindRoot((Object)new stats$index_attr_stats());
        Var var29 = const__55;
        var29.setMeta((IPersistentMap)const__57);
        Var var30 = var29;
        var29.bindRoot((Object)new stats$db_attr_stats());
        Var var31 = const__58;
        var31.setMeta((IPersistentMap)const__60);
        Var var32 = var31;
        var31.bindRoot((Object)new stats$db_stats());
        Var var33 = const__61;
        var33.setMeta((IPersistentMap)const__63);
        Var var34 = var33;
        var33.bindRoot((Object)new stats$index_attr_splits());
        Var var35 = const__64;
        var35.setMeta((IPersistentMap)const__66);
        Var var36 = var35;
        var35.bindRoot((Object)new stats$merge_data());
        Var var37 = const__67;
        var37.setMeta((IPersistentMap)const__69);
        Var var38 = var37;
        var37.bindRoot((Object)new stats$merge_splits());
        Var var39 = const__70;
        var39.setMeta((IPersistentMap)const__72);
        Var var40 = var39;
        var39.bindRoot((Object)new stats$db_attr_splits());
        Var var41 = const__73;
        var41.setMeta((IPersistentMap)const__75);
        Var var42 = var41;
        var41.bindRoot((Object)new stats$attr_stats_from_splits());
        Var var43 = const__76;
        var43.setMeta((IPersistentMap)const__78);
        Var var44 = var43;
        var43.bindRoot((Object)new stats$sizes());
        Var var45 = const__79;
        var45.setMeta((IPersistentMap)const__81);
        Var var46 = var45;
        var45.bindRoot((Object)new stats$datom_count());
        Var var47 = const__82;
        var47.setMeta((IPersistentMap)const__84);
        Var var48 = var47;
        var47.bindRoot((Object)new stats$index_datom_count());
        Var var49 = const__85;
        var49.setMeta((IPersistentMap)const__87);
        Var var50 = var49;
        var49.bindRoot((Object)new stats$segment_count());
        Var var51 = const__88;
        var51.setMeta((IPersistentMap)const__90);
        Var var52 = var51;
        var51.bindRoot((Object)new stats$sizes__GT_metrics());
        Var var53 = const__91;
        var53.setMeta((IPersistentMap)const__93);
        Var var54 = var53;
        var53.bindRoot((Object)new stats$index_metrics());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.stats");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.stats", (String)"sparse-v-count");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"des")))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.stats", (String)"key-summary");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"des")), Tuple.create((Object)Symbol.intern(null, (String)"des"), (Object)Symbol.intern(null, (String)"a-freqs?")))), RT.keyword(null, (String)"column"), 1});
        const__14 = RT.var((String)"datomic.stats", (String)"index-summary");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)Symbol.intern(null, (String)"index"), (Object)Symbol.intern(null, (String)"idx"), (Object)Symbol.intern(null, (String)"partfn")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.stats", (String)"avet");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"index")))), RT.keyword(null, (String)"column"), 1});
        const__20 = RT.var((String)"datomic.stats", (String)"aevt");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"index")))), RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.stats", (String)"eavt");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"index")))), RT.keyword(null, (String)"column"), 1});
        const__26 = RT.var((String)"datomic.stats", (String)"raet");
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"index")))), RT.keyword(null, (String)"column"), 1});
        const__29 = RT.var((String)"datomic.stats", (String)"total");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__32 = RT.var((String)"datomic.stats", (String)"datom-counts");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"summary-fn"), (Object)Symbol.intern(null, (String)"db")))), RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.stats", (String)"fulltext");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"index")))), RT.keyword(null, (String)"column"), 1});
        const__38 = RT.var((String)"datomic.stats", (String)"storage-tiers");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__43 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"mid-index"), RT.keyword(null, (String)"index"), RT.keyword(null, (String)"history")});
        const__44 = RT.var((String)"datomic.stats", (String)"memory-tiers");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__48 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"indexing"), RT.keyword(null, (String)"memidx")});
        const__49 = RT.var((String)"datomic.stats", (String)"tiers");
        const__50 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__51 = RT.var((String)"clojure.core", (String)"into");
        const__52 = RT.var((String)"datomic.stats", (String)"index-attr-stats");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)Symbol.intern(null, (String)"tier")))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.stats", (String)"db-attr-stats");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db")))), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.stats", (String)"db-stats");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db")))), RT.keyword(null, (String)"column"), 1});
        const__61 = RT.var((String)"datomic.stats", (String)"index-attr-splits");
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)Symbol.intern(null, (String)"tier"), (Object)Symbol.intern(null, (String)"attr")))), RT.keyword(null, (String)"column"), 1});
        const__64 = RT.var((String)"datomic.stats", (String)"merge-data");
        const__66 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cmp")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Comparator")})), (Object)Symbol.intern(null, (String)"ds1"), (Object)Symbol.intern(null, (String)"ds2")))), RT.keyword(null, (String)"column"), 1});
        const__67 = RT.var((String)"datomic.stats", (String)"merge-splits");
        const__69 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"splits")))), RT.keyword(null, (String)"column"), 1});
        const__70 = RT.var((String)"datomic.stats", (String)"db-attr-splits");
        const__72 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"attr")))), RT.keyword(null, (String)"column"), 1});
        const__73 = RT.var((String)"datomic.stats", (String)"attr-stats-from-splits");
        const__75 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db")))), RT.keyword(null, (String)"column"), 1});
        const__76 = RT.var((String)"datomic.stats", (String)"sizes");
        const__78 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"with-key-summary"))})))), RT.keyword(null, (String)"column"), 1});
        const__79 = RT.var((String)"datomic.stats", (String)"datom-count");
        const__81 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sizes")))), RT.keyword(null, (String)"column"), 1});
        const__82 = RT.var((String)"datomic.stats", (String)"index-datom-count");
        const__84 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sizes")))), RT.keyword(null, (String)"column"), 1});
        const__85 = RT.var((String)"datomic.stats", (String)"segment-count");
        const__87 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sizes")))), RT.keyword(null, (String)"column"), 1});
        const__88 = RT.var((String)"datomic.stats", (String)"sizes->metrics");
        const__90 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__91 = RT.var((String)"datomic.stats", (String)"index-metrics");
        const__93 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        stats__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.stats__init").getClassLoader());
        try {
            stats__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


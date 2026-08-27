/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AReference
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AReference;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.fulltext$add_chunked_data_to_writer;
import datomic.fulltext$add_data_to_writer;
import datomic.fulltext$build_index;
import datomic.fulltext$cluster_directory;
import datomic.fulltext$clustered_fulltext;
import datomic.fulltext$create_index_on_dir;
import datomic.fulltext$create_indexing_job;
import datomic.fulltext$do_indexing_job;
import datomic.fulltext$doc__GT_datum;
import datomic.fulltext$find_historic_docid;
import datomic.fulltext$find_matching_assertion;
import datomic.fulltext$fn__14520;
import datomic.fulltext$fn__14523;
import datomic.fulltext$fn__14528;
import datomic.fulltext$fn__14633;
import datomic.fulltext$fn__14724;
import datomic.fulltext$fulltext_index_reader;
import datomic.fulltext$hybrid_dir;
import datomic.fulltext$index_files;
import datomic.fulltext$loading__6434__auto____14518;
import datomic.fulltext$promote_to_cluster;
import datomic.fulltext$reify__14547;
import datomic.fulltext$reify__14653;
import datomic.fulltext$reify__14655;
import datomic.fulltext$remove_data_from_reader;
import datomic.fulltext$search;
import datomic.fulltext$search_iterable;
import datomic.fulltext$separate_history;
import datomic.fulltext$write_changed_val;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class fulltext__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__4;
    public static final AFn const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final AFn const__8;
    public static final Var const__9;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Object const__21;
    public static final Var const__22;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__27;
    public static final Var const__28;
    public static final AFn const__30;
    public static final Var const__31;
    public static final AFn const__33;
    public static final Var const__34;
    public static final AFn const__35;
    public static final AFn const__39;
    public static final Var const__40;
    public static final AFn const__41;
    public static final Var const__42;
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
    public static final AFn const__63;
    public static final Var const__64;
    public static final AFn const__65;
    public static final Var const__66;
    public static final Var const__67;
    public static final Object const__68;
    public static final AFn const__71;
    public static final Var const__72;
    public static final AFn const__73;
    public static final Var const__74;
    public static final AFn const__77;
    public static final Var const__78;
    public static final AFn const__80;
    public static final Var const__81;
    public static final AFn const__83;
    public static final Var const__84;
    public static final AFn const__86;
    public static final Var const__87;
    public static final AFn const__89;
    public static final Var const__90;
    public static final AFn const__92;
    public static final Var const__93;
    public static final AFn const__95;
    public static final Var const__96;
    public static final AFn const__98;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__4);
        Object object2 = ((IFn)new fulltext$loading__6434__auto____14518()).invoke();
        if (((Symbol)const__1).equals((Object)const__5)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new fulltext$fn__14520())));
            v3 = null;
        }
        Object object3 = const__6.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)const__7.getRawRoot()).invoke((Object)const__8);
        Var var = const__9;
        var.setMeta((IPersistentMap)const__14);
        Var var2 = var;
        var.bindRoot((Object)new fulltext$doc__GT_datum());
        Object object5 = ((IFn)new fulltext$fn__14523()).invoke();
        Object object6 = ((IFn)new fulltext$fn__14528()).invoke();
        Var var3 = const__15;
        var3.setMeta((IPersistentMap)const__17);
        Var var4 = var3;
        var3.bindRoot((Object)new fulltext$search_iterable());
        Var var5 = const__18;
        var5.setMeta((IPersistentMap)const__20);
        Var var6 = var5;
        var5.bindRoot(const__21);
        Var var7 = const__22;
        var7.setMeta((IPersistentMap)const__24);
        Var var8 = var7;
        var7.bindRoot((Object)new fulltext$hybrid_dir());
        Var var9 = const__25;
        var9.setMeta((IPersistentMap)const__27);
        Var var10 = var9;
        var9.bindRoot((Object)new fulltext$index_files());
        Var var11 = const__28;
        var11.setMeta((IPersistentMap)const__30);
        Var var12 = var11;
        var11.bindRoot((Object)new fulltext$promote_to_cluster());
        Var var13 = const__31;
        var13.setMeta((IPersistentMap)const__33);
        Var var14 = var13;
        var13.bindRoot((Object)new fulltext$cluster_directory());
        Var var15 = const__34;
        var15.setMeta((IPersistentMap)const__35);
        Var var16 = var15;
        var15.bindRoot(((IFn)const__31.getRawRoot()).invoke((Object)((IObj)new fulltext$reify__14547(null)).withMeta((IPersistentMap)const__39), null));
        Var var17 = const__40;
        var17.setMeta((IPersistentMap)const__41);
        Var var18 = var17;
        var17.bindRoot(((IFn)const__42.getRawRoot()).invoke((Object)"tmp/search"));
        Var var19 = const__43;
        var19.setMeta((IPersistentMap)const__45);
        Var var20 = var19;
        var19.bindRoot((Object)new fulltext$create_indexing_job());
        Var var21 = const__46;
        var21.setMeta((IPersistentMap)const__48);
        Var var22 = var21;
        var21.bindRoot((Object)new fulltext$add_data_to_writer());
        Var var23 = const__49;
        var23.setMeta((IPersistentMap)const__51);
        Var var24 = var23;
        var23.bindRoot((Object)new fulltext$add_chunked_data_to_writer());
        Var var25 = const__52;
        var25.setMeta((IPersistentMap)const__54);
        Var var26 = var25;
        var25.bindRoot((Object)new fulltext$find_historic_docid());
        Var var27 = const__55;
        var27.setMeta((IPersistentMap)const__57);
        Var var28 = var27;
        var27.bindRoot((Object)new fulltext$remove_data_from_reader());
        Var var29 = const__58;
        var29.setMeta((IPersistentMap)const__60);
        Var var30 = var29;
        var29.bindRoot((Object)new fulltext$create_index_on_dir());
        Var var31 = const__61;
        var31.setMeta((IPersistentMap)const__63);
        Var var32 = var31;
        var31.bindRoot((Object)new fulltext$do_indexing_job());
        Object object7 = ((IFn)new fulltext$fn__14633()).invoke();
        Var var33 = const__64;
        var33.setMeta((IPersistentMap)const__65);
        Var var34 = var33;
        var33.bindRoot(((IFn)const__66.getRawRoot()).invoke(const__67.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__68, RT.mapUniqueKeys((Object[])new Object[]{"search-root", ((IObj)new fulltext$reify__14653(null)).withMeta((IPersistentMap)const__71)})})));
        Var var35 = const__72;
        var35.setMeta((IPersistentMap)const__73);
        Var var36 = var35;
        var35.bindRoot(((IFn)const__66.getRawRoot()).invoke(const__74.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{"search-root", ((IObj)new fulltext$reify__14655(null)).withMeta((IPersistentMap)const__77)})));
        Var var37 = const__78;
        var37.setMeta((IPersistentMap)const__80);
        Var var38 = var37;
        var37.bindRoot((Object)new fulltext$write_changed_val());
        Var var39 = const__81;
        var39.setMeta((IPersistentMap)const__83);
        Var var40 = var39;
        var39.bindRoot((Object)new fulltext$find_matching_assertion());
        Var var41 = const__84;
        var41.setMeta((IPersistentMap)const__86);
        Var var42 = var41;
        var41.bindRoot((Object)new fulltext$separate_history());
        Var var43 = const__87;
        var43.setMeta((IPersistentMap)const__89);
        Var var44 = var43;
        var43.bindRoot((Object)new fulltext$build_index());
        Object object8 = ((IFn)new fulltext$fn__14724()).invoke();
        Var var45 = const__90;
        var45.setMeta((IPersistentMap)const__92);
        Var var46 = var45;
        var45.bindRoot((Object)new fulltext$clustered_fulltext());
        Var var47 = const__93;
        var47.setMeta((IPersistentMap)const__95);
        Var var48 = var47;
        var47.bindRoot((Object)new fulltext$fulltext_index_reader());
        Var var49 = const__96;
        var49.setMeta((IPersistentMap)const__98);
        Var var50 = var49;
        var49.bindRoot((Object)new fulltext$search());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.fulltext")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"author"), "Stuart Halloway"}));
        const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "Fulltext search. Most of the code in this namespace is used\nby the master and the peers to build indices. The only consumer-facing\nAPI is search.", RT.keyword(null, (String)"author"), "Stuart Halloway"});
        const__5 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__6 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__7 = RT.var((String)"clojure.core", (String)"use");
        const__8 = (AFn)Symbol.intern(null, (String)"clojure.pprint");
        const__9 = RT.var((String)"datomic.fulltext", (String)"doc->datum");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"doc"), (Object)((IObj)Symbol.intern(null, (String)"a")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.fulltext", (String)"search-iterable");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"searcher")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexSearcher")})), (Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"attr"), (Object)Symbol.intern(null, (String)"search-map")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.fulltext", (String)"default-chunk-size");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__21 = 54000L;
        const__22 = RT.var((String)"datomic.fulltext", (String)"hybrid-dir");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"writer"), (Object)Symbol.intern(null, (String)"reader"), (Object)Symbol.intern(null, (String)"delete-handler")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.fulltext", (String)"index-files");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"index-dir")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.fulltext", (String)"promote-to-cluster");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"indexing-job")))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.fulltext", (String)"cluster-directory");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"clusterfs"), (Object)Symbol.intern(null, (String)"olookup")))), RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"datomic.fulltext", (String)"null-dir");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 161, RT.keyword(null, (String)"column"), 4});
        const__40 = RT.var((String)"datomic.fulltext", (String)"work-dir");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__42 = RT.var((String)"clojure.core", (String)"atom");
        const__43 = RT.var((String)"datomic.fulltext", (String)"create-indexing-job");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cstore"), (Object)Symbol.intern(null, (String)"olookup"), (Object)Symbol.intern(null, (String)"baseid")))), RT.keyword(null, (String)"column"), 1});
        const__46 = RT.var((String)"datomic.fulltext", (String)"add-data-to-writer");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"writer"), (Object)Symbol.intern(null, (String)"data")))), RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"datomic.fulltext", (String)"add-chunked-data-to-writer");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"writer"), (Object)Symbol.intern(null, (String)"data")))), RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.fulltext", (String)"find-historic-docid");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"searcher")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexSearcher")})), (Object)((IObj)Symbol.intern(null, (String)"datum")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDatum")}))))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.fulltext", (String)"remove-data-from-reader");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexReader")})), (Object)Symbol.intern(null, (String)"data")))), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.fulltext", (String)"create-index-on-dir");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"dir"), (Object)Symbol.intern(null, (String)"add-data"), (Object)Symbol.intern(null, (String)"remove-data")))), RT.keyword(null, (String)"column"), 1});
        const__61 = RT.var((String)"datomic.fulltext", (String)"do-indexing-job");
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cstore"), (Object)Symbol.intern(null, (String)"olookup"), (Object)Symbol.intern(null, (String)"add-data"), (Object)Symbol.intern(null, (String)"remove-data"), (Object)Symbol.intern(null, (String)"attr-id"), (Object)Symbol.intern(null, (String)"dirid")))), RT.keyword(null, (String)"column"), 1});
        const__64 = RT.var((String)"datomic.fulltext", (String)"write-handlers");
        const__65 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__66 = RT.var((String)"clojure.core", (String)"merge");
        const__67 = RT.var((String)"datomic.clusterfs", (String)"write-handlers");
        const__68 = RT.classForName((String)"datomic.fulltext.Root");
        const__71 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 260, RT.keyword(null, (String)"column"), 6});
        const__72 = RT.var((String)"datomic.fulltext", (String)"read-handlers");
        const__73 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__74 = RT.var((String)"datomic.clusterfs", (String)"read-handlers");
        const__77 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 270, RT.keyword(null, (String)"column"), 5});
        const__78 = RT.var((String)"datomic.fulltext", (String)"write-changed-val");
        const__80 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cstore"), (Object)Symbol.intern(null, (String)"oldid"), (Object)Symbol.intern(null, (String)"oldval"), (Object)Symbol.intern(null, (String)"newval"), (Object)Symbol.intern(null, (String)"garbage")))), RT.keyword(null, (String)"column"), 1});
        const__81 = RT.var((String)"datomic.fulltext", (String)"find-matching-assertion");
        const__83 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDb")})), (Object)((IObj)Symbol.intern(null, (String)"d")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDatum")}))))), RT.keyword(null, (String)"column"), 1});
        const__84 = RT.var((String)"datomic.fulltext", (String)"separate-history");
        const__86 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"data"), (Object)Symbol.intern(null, (String)"history")))), RT.keyword(null, (String)"column"), 1});
        const__87 = RT.var((String)"datomic.fulltext", (String)"build-index");
        const__89 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"cstore"), Symbol.intern(null, (String)"olookup"), Symbol.intern(null, (String)"db"), Symbol.intern(null, (String)"aevt"), Symbol.intern(null, (String)"attrids"), Symbol.intern(null, (String)"old-root-id"), Symbol.intern(null, (String)"old-hist-id")}))), RT.keyword(null, (String)"column"), 1});
        const__90 = RT.var((String)"datomic.fulltext", (String)"clustered-fulltext");
        const__92 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"olookup"), (Object)Symbol.intern(null, (String)"rootid")))), RT.keyword(null, (String)"column"), 1});
        const__93 = RT.var((String)"datomic.fulltext", (String)"fulltext-index-reader");
        const__95 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"idx"), (Object)Symbol.intern(null, (String)"attrid")))), RT.keyword(null, (String)"column"), 1});
        const__96 = RT.var((String)"datomic.fulltext", (String)"search");
        const__98 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"a"), (Object)Symbol.intern(null, (String)"search-map"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.Iterable")})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        fulltext__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.fulltext__init").getClassLoader());
        try {
            fulltext__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


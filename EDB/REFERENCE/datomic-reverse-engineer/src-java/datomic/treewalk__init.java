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
import datomic.treewalk$create_ids__GT_nodes;
import datomic.treewalk$create_node;
import datomic.treewalk$create_parent_node;
import datomic.treewalk$db_seq;
import datomic.treewalk$fn__19659;
import datomic.treewalk$fn__19663;
import datomic.treewalk$fn__19666;
import datomic.treewalk$fn__19681;
import datomic.treewalk$fn__19684;
import datomic.treewalk$fn__19697;
import datomic.treewalk$fn__19708;
import datomic.treewalk$fn__19710;
import datomic.treewalk$fn__19712;
import datomic.treewalk$fn__19714;
import datomic.treewalk$fn__19716;
import datomic.treewalk$fn__19718;
import datomic.treewalk$fn__19720;
import datomic.treewalk$fn__19722;
import datomic.treewalk$fn__19724;
import datomic.treewalk$index_top_walker;
import datomic.treewalk$index_tree_seq;
import datomic.treewalk$loading__6434__auto____19657;
import datomic.treewalk$log_root_walker;
import datomic.treewalk$log_tree_seq;
import datomic.treewalk$lookup_val;
import datomic.treewalk$tree_node_ids;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class treewalk__init {
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
    public static final Keyword const__37;
    public static final Keyword const__38;
    public static final AFn const__39;
    public static final AFn const__40;
    public static final AFn const__41;
    public static final AFn const__42;
    public static final AFn const__43;
    public static final AFn const__44;
    public static final Var const__45;
    public static final AFn const__48;
    public static final AFn const__67;
    public static final Var const__68;
    public static final Object const__69;
    public static final Object const__70;
    public static final Object const__71;
    public static final Object const__72;
    public static final Var const__73;
    public static final AFn const__76;
    public static final Var const__77;
    public static final AFn const__79;
    public static final Var const__80;
    public static final AFn const__82;
    public static final Var const__83;
    public static final AFn const__85;
    public static final Var const__86;
    public static final AFn const__88;
    public static final Var const__89;
    public static final AFn const__91;
    public static final Var const__92;
    public static final AFn const__94;
    public static final Var const__95;
    public static final AFn const__97;
    public static final Var const__98;
    public static final AFn const__100;
    public static final Var const__101;
    public static final AFn const__103;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new treewalk$loading__6434__auto____19657()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new treewalk$fn__19659())));
            v2 = null;
        }
        Object object3 = ((IFn)new treewalk$fn__19663()).invoke();
        Object object4 = const__3;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, const__6.getRawRoot(), (Object)const__7, null);
        Object object6 = ((IFn)const__8).invoke((Object)const__5, (Object)const__9);
        Object object7 = ((IFn)const__10.getRawRoot()).invoke((Object)const__5, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__5, (Object)const__19, (Object)const__21, (Object)const__22, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__26, ((IFn)const__11.getRawRoot()).invoke((Object)const__27, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__5})))), new treewalk$fn__19666()})));
        Object object8 = ((IFn)const__29.getRawRoot()).invoke(const__5.getRawRoot());
        AFn aFn = const__30;
        Object object9 = ((IFn)new treewalk$fn__19681()).invoke();
        Object object10 = const__31;
        Object object11 = ((IFn)const__4.getRawRoot()).invoke((Object)const__32, const__6.getRawRoot(), (Object)const__7, null);
        Object object12 = ((IFn)const__8).invoke((Object)const__32, (Object)const__33);
        Object object13 = ((IFn)const__10.getRawRoot()).invoke((Object)const__32, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__35, (Object)const__16, (Object)const__36, (Object)const__18, (Object)const__32, (Object)const__19, (Object)const__39, (Object)const__22, (Object)RT.map((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__40, ((IFn)const__11.getRawRoot()).invoke((Object)const__41, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__32})))), new treewalk$fn__19684(), ((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__42, ((IFn)const__11.getRawRoot()).invoke((Object)const__43, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__32})))), new treewalk$fn__19697()})));
        Object object14 = ((IFn)const__29.getRawRoot()).invoke(const__32.getRawRoot());
        AFn aFn2 = const__44;
        Var var = const__45;
        var.setMeta((IPersistentMap)const__48);
        Var var2 = var;
        var.bindRoot((Object)const__67);
        Object object15 = ((IFn)const__68.getRawRoot()).invoke(const__69, const__32.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__37, new treewalk$fn__19708(), const__38, new treewalk$fn__19710()}));
        Object object16 = ((IFn)const__68.getRawRoot()).invoke(const__70, const__32.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__37, new treewalk$fn__19712(), const__38, new treewalk$fn__19714()}));
        Object object17 = ((IFn)const__68.getRawRoot()).invoke(const__71, const__32.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__37, new treewalk$fn__19716(), const__38, new treewalk$fn__19718()}));
        Object object18 = ((IFn)const__68.getRawRoot()).invoke(const__72, const__32.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{const__37, new treewalk$fn__19720(), const__38, new treewalk$fn__19722()}));
        Object object19 = ((IFn)new treewalk$fn__19724()).invoke();
        Var var3 = const__73;
        var3.setMeta((IPersistentMap)const__76);
        Var var4 = var3;
        var3.bindRoot((Object)new treewalk$lookup_val());
        Var var5 = const__77;
        var5.setMeta((IPersistentMap)const__79);
        Var var6 = var5;
        var5.bindRoot((Object)new treewalk$create_node());
        Var var7 = const__80;
        var7.setMeta((IPersistentMap)const__82);
        Var var8 = var7;
        var7.bindRoot((Object)new treewalk$create_ids__GT_nodes());
        Var var9 = const__83;
        var9.setMeta((IPersistentMap)const__85);
        Var var10 = var9;
        var9.bindRoot((Object)new treewalk$create_parent_node());
        Var var11 = const__86;
        var11.setMeta((IPersistentMap)const__88);
        Var var12 = var11;
        var11.bindRoot((Object)new treewalk$tree_node_ids());
        Var var13 = const__89;
        var13.setMeta((IPersistentMap)const__91);
        Var var14 = var13;
        var13.bindRoot((Object)new treewalk$index_top_walker());
        Var var15 = const__92;
        var15.setMeta((IPersistentMap)const__94);
        Var var16 = var15;
        var15.bindRoot((Object)new treewalk$index_tree_seq());
        Var var17 = const__95;
        var17.setMeta((IPersistentMap)const__97);
        Var var18 = var17;
        var17.bindRoot((Object)new treewalk$log_root_walker());
        Var var19 = const__98;
        var19.setMeta((IPersistentMap)const__100);
        Var var20 = var19;
        var19.bindRoot((Object)new treewalk$log_tree_seq());
        Var var21 = const__101;
        var21.setMeta((IPersistentMap)const__103);
        Var var22 = var21;
        var21.bindRoot((Object)new treewalk$db_seq());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.treewalk");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.classForName((String)"datomic.treewalk.NodeId");
        const__4 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__5 = RT.var((String)"datomic.treewalk", (String)"NodeId");
        const__6 = RT.var((String)"clojure.core", (String)"assoc");
        const__7 = RT.keyword(null, (String)"doc");
        const__8 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__9 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"node-id")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__10 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__11 = RT.var((String)"clojure.core", (String)"merge");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.treewalk.NodeId"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.treewalk.NodeId")});
        const__16 = RT.keyword(null, (String)"sigs");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"node-id"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"node-id")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Return storage id of this node."})});
        const__18 = RT.keyword(null, (String)"var");
        const__19 = RT.keyword(null, (String)"method-map");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"node-id"), RT.keyword(null, (String)"node-id")});
        const__22 = RT.keyword(null, (String)"method-builders");
        const__23 = RT.var((String)"clojure.core", (String)"intern");
        const__24 = RT.var((String)"clojure.core", (String)"*ns*");
        const__25 = RT.var((String)"clojure.core", (String)"with-meta");
        const__26 = (AFn)((IObj)Symbol.intern(null, (String)"node-id")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"node-id")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Return storage id of this node."});
        const__28 = RT.keyword(null, (String)"protocol");
        const__29 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__30 = (AFn)Symbol.intern(null, (String)"NodeId");
        const__31 = RT.classForName((String)"datomic.treewalk.TreeWalker");
        const__32 = RT.var((String)"datomic.treewalk", (String)"TreeWalker");
        const__33 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"child-node-ids")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), ((IObj)Symbol.intern(null, (String)"subtrees")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"ids->nodes"))))}))));
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.treewalk.TreeWalker"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.treewalk.TreeWalker")});
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"child-node-ids"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"child-node-ids")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Ids of nodes owned by this node."}), RT.keyword(null, (String)"subtrees"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"subtrees")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"ids->nodes"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"ids->nodes")))), RT.keyword(null, (String)"doc"), "Subtree objects for child nodes that contain more nodes.\n    Subtrees implement TreeWalker. Nil if children are leaves."})});
        const__37 = RT.keyword(null, (String)"child-node-ids");
        const__38 = RT.keyword(null, (String)"subtrees");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"child-node-ids"), RT.keyword(null, (String)"child-node-ids"), RT.keyword(null, (String)"subtrees"), RT.keyword(null, (String)"subtrees")});
        const__40 = (AFn)((IObj)Symbol.intern(null, (String)"subtrees")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"ids->nodes"))))}));
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"subtrees")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"ids->nodes"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"ids->nodes")))), RT.keyword(null, (String)"doc"), "Subtree objects for child nodes that contain more nodes.\n    Subtrees implement TreeWalker. Nil if children are leaves."});
        const__42 = (AFn)((IObj)Symbol.intern(null, (String)"child-node-ids")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"child-node-ids")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Ids of nodes owned by this node."});
        const__44 = (AFn)Symbol.intern(null, (String)"TreeWalker");
        const__45 = RT.var((String)"datomic.treewalk", (String)"index-root-keys");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__67 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"aevt-hist"), RT.keyword(null, (String)"aevt"), RT.keyword(null, (String)"aevt-mid"), RT.keyword(null, (String)"aevt-main"), RT.keyword(null, (String)"eavt-hist"), RT.keyword(null, (String)"eavt"), RT.keyword(null, (String)"eavt-mid"), RT.keyword(null, (String)"eavt-main"), RT.keyword(null, (String)"raet-hist"), RT.keyword(null, (String)"raet"), RT.keyword(null, (String)"raet-mid"), RT.keyword(null, (String)"raet-main"), RT.keyword(null, (String)"avet-hist"), RT.keyword(null, (String)"avet"), RT.keyword(null, (String)"avet-mid"), RT.keyword(null, (String)"avet-main"), RT.keyword(null, (String)"fulltext-hist"), RT.keyword(null, (String)"fulltext")});
        const__68 = RT.var((String)"clojure.core", (String)"extend");
        const__69 = RT.classForName((String)"datomic.fulltext.Root");
        const__70 = RT.classForName((String)"datomic.clusterfs.ClusterFS");
        const__71 = RT.classForName((String)"datomic.index.RootNode");
        const__72 = RT.classForName((String)"datomic.index.DirNode");
        const__73 = RT.var((String)"datomic.treewalk", (String)"lookup-val");
        const__76 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"allow-missing?")))), RT.keyword(null, (String)"column"), 1});
        const__77 = RT.var((String)"datomic.treewalk", (String)"create-node");
        const__79 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"allow-missing?"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"pre"), Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"lookup"))})))), RT.keyword(null, (String)"column"), 1});
        const__80 = RT.var((String)"datomic.treewalk", (String)"create-ids->nodes");
        const__82 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"lookup")), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"allow-missing?"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"pre"), Tuple.create((Object)Symbol.intern(null, (String)"lookup"))})))), RT.keyword(null, (String)"column"), 1});
        const__83 = RT.var((String)"datomic.treewalk", (String)"create-parent-node");
        const__85 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"walker"), (Object)Symbol.intern(null, (String)"lookup")), Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"walker"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"allow-missing?")))), RT.keyword(null, (String)"column"), 1});
        const__86 = RT.var((String)"datomic.treewalk", (String)"tree-node-ids");
        const__88 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"node"), (Object)Symbol.intern(null, (String)"ids->nodes")))), RT.keyword(null, (String)"column"), 1});
        const__89 = RT.var((String)"datomic.treewalk", (String)"index-top-walker");
        const__91 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"top")))), RT.keyword(null, (String)"column"), 1});
        const__92 = RT.var((String)"datomic.treewalk", (String)"index-tree-seq");
        const__94 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"lookup")), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"allow-missing?"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"pre"), Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"lookup"))})))), RT.keyword(null, (String)"column"), 1});
        const__95 = RT.var((String)"datomic.treewalk", (String)"log-root-walker");
        const__97 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"root"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"allow-missing?")))), RT.keyword(null, (String)"column"), 1});
        const__98 = RT.var((String)"datomic.treewalk", (String)"log-tree-seq");
    }

    public static void __init1() {
        const__100 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"lookup")), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"lookup"), (Object)Symbol.intern(null, (String)"allow-missing?"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"pre"), Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"lookup"))})))), RT.keyword(null, (String)"column"), 1});
        const__101 = RT.var((String)"datomic.treewalk", (String)"db-seq");
        const__103 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"log-root-node"), (Object)Symbol.intern(null, (String)"index-top-node"), (Object)Symbol.intern(null, (String)"lookup")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        treewalk__init.__init0();
        treewalk__init.__init1();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.treewalk__init").getClassLoader());
        try {
            treewalk__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


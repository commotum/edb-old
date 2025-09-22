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
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.LockingTransaction
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentHashSet
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
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.Namespace;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.excise$a_target_QMARK_;
import datomic.excise$component_attr_QMARK_;
import datomic.excise$component_es_set;
import datomic.excise$create_a__GT_xpreds;
import datomic.excise$create_as_pred;
import datomic.excise$create_e__GT_xpreds;
import datomic.excise$create_es_pred;
import datomic.excise$create_xpreds;
import datomic.excise$datoms;
import datomic.excise$e_target_QMARK_;
import datomic.excise$fn__14758;
import datomic.excise$fn__14780;
import datomic.excise$fn__14783;
import datomic.excise$fn__14796;
import datomic.excise$get_before_t;
import datomic.excise$keeper_QMARK_;
import datomic.excise$loading__6434__auto____14756;
import datomic.excise$pred;
import datomic.excise$pred_and_extent;
import datomic.excise$ref_datom_QMARK_;
import datomic.excise$remove_QMARK_;
import datomic.excise$target;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class excise__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final Keyword const__2;
    public static final AFn const__4;
    public static final AFn const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__15;
    public static final Object const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final ISeq const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final AFn const__27;
    public static final Keyword const__28;
    public static final AFn const__29;
    public static final Keyword const__30;
    public static final Keyword const__31;
    public static final AFn const__34;
    public static final Keyword const__35;
    public static final Var const__36;
    public static final Var const__37;
    public static final Var const__38;
    public static final AFn const__39;
    public static final AFn const__40;
    public static final Keyword const__41;
    public static final AFn const__42;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__45;
    public static final Var const__46;
    public static final AFn const__48;
    public static final Var const__49;
    public static final Var const__50;
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
    public static final Var const__94;
    public static final AFn const__96;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__4);
        Object object2 = ((IFn)new excise$loading__6434__auto____14756()).invoke();
        if (((Symbol)const__1).equals((Object)const__5)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new excise$fn__14758())));
            v3 = null;
        }
        Object object3 = const__6.set((Object)Boolean.TRUE);
        Var var = const__7;
        var.setMeta((IPersistentMap)const__12);
        Var var2 = var;
        var.bindRoot((Object)new excise$component_attr_QMARK_());
        Var var3 = const__13;
        var3.setMeta((IPersistentMap)const__15);
        Var var4 = var3;
        var3.bindRoot((Object)new excise$component_es_set());
        Object object4 = ((IFn)new excise$fn__14780()).invoke();
        Object object5 = const__16;
        Object object6 = ((IFn)const__17.getRawRoot()).invoke((Object)const__18, const__19.getRawRoot(), (Object)const__2, null);
        Object object7 = ((IFn)const__20).invoke((Object)const__18, (Object)const__21);
        Object object8 = ((IFn)const__22.getRawRoot()).invoke((Object)const__18, const__23.getRawRoot(), ((IFn)const__19.getRawRoot()).invoke((Object)const__27, (Object)const__28, (Object)const__29, (Object)const__30, (Object)const__18, (Object)const__31, (Object)const__34, (Object)const__35, (Object)RT.map((Object[])new Object[]{((IFn)const__36.getRawRoot()).invoke(const__37.get(), ((IFn)const__38.getRawRoot()).invoke((Object)const__39, ((IFn)const__23.getRawRoot()).invoke((Object)const__40, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__41, const__18})))), new excise$fn__14783(), ((IFn)const__36.getRawRoot()).invoke(const__37.get(), ((IFn)const__38.getRawRoot()).invoke((Object)const__42, ((IFn)const__23.getRawRoot()).invoke((Object)const__43, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__41, const__18})))), new excise$fn__14796()})));
        Object object9 = ((IFn)const__44.getRawRoot()).invoke(const__18.getRawRoot());
        AFn aFn = const__45;
        Var var5 = const__46;
        var5.setMeta((IPersistentMap)const__48);
        Var var6 = var5;
        var5.bindRoot(((IFn)const__49.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__50.getRawRoot()).invoke(const__51.getRawRoot())));
        Var var7 = const__52;
        var7.setMeta((IPersistentMap)const__54);
        Var var8 = var7;
        var7.bindRoot((Object)new excise$keeper_QMARK_());
        Var var9 = const__55;
        var9.setMeta((IPersistentMap)const__57);
        Var var10 = var9;
        var9.bindRoot((Object)new excise$get_before_t());
        Var var11 = const__58;
        var11.setMeta((IPersistentMap)const__60);
        Var var12 = var11;
        var11.bindRoot((Object)new excise$pred_and_extent());
        Var var13 = const__61;
        var13.setMeta((IPersistentMap)const__63);
        Var var14 = var13;
        var13.bindRoot((Object)new excise$pred());
        Var var15 = const__64;
        var15.setMeta((IPersistentMap)const__66);
        Var var16 = var15;
        var15.bindRoot((Object)new excise$e_target_QMARK_());
        Var var17 = const__67;
        var17.setMeta((IPersistentMap)const__69);
        Var var18 = var17;
        var17.bindRoot((Object)new excise$a_target_QMARK_());
        Var var19 = const__70;
        var19.setMeta((IPersistentMap)const__72);
        Var var20 = var19;
        var19.bindRoot((Object)new excise$ref_datom_QMARK_());
        Var var21 = const__73;
        var21.setMeta((IPersistentMap)const__75);
        Var var22 = var21;
        var21.bindRoot((Object)new excise$target());
        Var var23 = const__76;
        var23.setMeta((IPersistentMap)const__78);
        Var var24 = var23;
        var23.bindRoot((Object)new excise$create_e__GT_xpreds());
        Var var25 = const__79;
        var25.setMeta((IPersistentMap)const__81);
        Var var26 = var25;
        var25.bindRoot((Object)new excise$create_es_pred());
        Var var27 = const__82;
        var27.setMeta((IPersistentMap)const__84);
        Var var28 = var27;
        var27.bindRoot((Object)new excise$create_a__GT_xpreds());
        Var var29 = const__85;
        var29.setMeta((IPersistentMap)const__87);
        Var var30 = var29;
        var29.bindRoot((Object)new excise$create_as_pred());
        Var var31 = const__88;
        var31.setMeta((IPersistentMap)const__90);
        Var var32 = var31;
        var31.bindRoot((Object)new excise$create_xpreds());
        Var var33 = const__91;
        var33.setMeta((IPersistentMap)const__93);
        Var var34 = var33;
        var33.bindRoot((Object)new excise$datoms());
        Var var35 = const__94;
        var35.setMeta((IPersistentMap)const__96);
        Var var36 = var35;
        var35.bindRoot((Object)new excise$remove_QMARK_());
        Object v48 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.excise")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"author"), "Rich Hickey"}));
        const__2 = RT.keyword(null, (String)"doc");
        const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "Excise utilities", RT.keyword(null, (String)"author"), "Rich Hickey"});
        const__5 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__6 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__7 = RT.var((String)"datomic.excise", (String)"component-attr?");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"a")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.excise", (String)"component-es-set");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e")), Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"e"), (Object)Symbol.intern(null, (String)"via-attrs")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.classForName((String)"datomic.excise.ExcisePred");
        const__17 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__18 = RT.var((String)"datomic.excise", (String)"ExcisePred");
        const__19 = RT.var((String)"clojure.core", (String)"assoc");
        const__20 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__21 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"ep-datoms")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"))))})), ((IObj)Symbol.intern(null, (String)"ep-remove?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"), (Object)Symbol.intern(null, (String)"datom"))))}))));
        const__22 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__23 = RT.var((String)"clojure.core", (String)"merge");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.excise.ExcisePred"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.excise.ExcisePred")});
        const__28 = RT.keyword(null, (String)"sigs");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"ep-datoms"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"ep-datoms")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred")))), RT.keyword(null, (String)"doc"), null}), RT.keyword(null, (String)"ep-remove?"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"ep-remove?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"), (Object)Symbol.intern(null, (String)"datom"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"), (Object)Symbol.intern(null, (String)"datom")))), RT.keyword(null, (String)"doc"), null})});
        const__30 = RT.keyword(null, (String)"var");
        const__31 = RT.keyword(null, (String)"method-map");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"ep-datoms"), RT.keyword(null, (String)"ep-datoms"), RT.keyword(null, (String)"ep-remove?"), RT.keyword(null, (String)"ep-remove?")});
        const__35 = RT.keyword(null, (String)"method-builders");
        const__36 = RT.var((String)"clojure.core", (String)"intern");
        const__37 = RT.var((String)"clojure.core", (String)"*ns*");
        const__38 = RT.var((String)"clojure.core", (String)"with-meta");
        const__39 = (AFn)((IObj)Symbol.intern(null, (String)"ep-remove?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"), (Object)Symbol.intern(null, (String)"datom"))))}));
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"ep-remove?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"), (Object)Symbol.intern(null, (String)"datom"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"), (Object)Symbol.intern(null, (String)"datom")))), RT.keyword(null, (String)"doc"), null});
        const__41 = RT.keyword(null, (String)"protocol");
        const__42 = (AFn)((IObj)Symbol.intern(null, (String)"ep-datoms")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"))))}));
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"ep-datoms")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred")))), RT.keyword(null, (String)"doc"), null});
        const__44 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__45 = (AFn)Symbol.intern(null, (String)"ExcisePred");
        const__46 = RT.var((String)"datomic.excise", (String)"bootids");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"clojure.core", (String)"into");
        const__50 = RT.var((String)"clojure.core", (String)"vals");
        const__51 = RT.var((String)"datomic.db", (String)"BOOT-IDS");
        const__52 = RT.var((String)"datomic.excise", (String)"keeper?");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"d")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDatum")}))))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.excise", (String)"get-before-t");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.excise", (String)"pred-and-extent");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"db")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Database")})), (Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__61 = RT.var((String)"datomic.excise", (String)"pred");
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__64 = RT.var((String)"datomic.excise", (String)"e-target?");
        const__66 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__67 = RT.var((String)"datomic.excise", (String)"a-target?");
        const__69 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__70 = RT.var((String)"datomic.excise", (String)"ref-datom?");
        const__72 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)((IObj)Symbol.intern(null, (String)"d")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDatum")}))))), RT.keyword(null, (String)"column"), 1});
        const__73 = RT.var((String)"datomic.excise", (String)"target");
        const__75 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"spec")))), RT.keyword(null, (String)"column"), 1});
        const__76 = RT.var((String)"datomic.excise", (String)"create-e->xpreds");
        const__78 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"specs")))), RT.keyword(null, (String)"column"), 1});
        const__79 = RT.var((String)"datomic.excise", (String)"create-es-pred");
        const__81 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"specs")))), RT.keyword(null, (String)"column"), 1});
        const__82 = RT.var((String)"datomic.excise", (String)"create-a->xpreds");
        const__84 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"specs")))), RT.keyword(null, (String)"column"), 1});
        const__85 = RT.var((String)"datomic.excise", (String)"create-as-pred");
        const__87 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"specs")))), RT.keyword(null, (String)"column"), 1});
        const__88 = RT.var((String)"datomic.excise", (String)"create-xpreds");
        const__90 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"specs")))), RT.keyword(null, (String)"column"), 1});
        const__91 = RT.var((String)"datomic.excise", (String)"datoms");
        const__93 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred")))), RT.keyword(null, (String)"column"), 1});
        const__94 = RT.var((String)"datomic.excise", (String)"remove?");
        const__96 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"epred"), (Object)Symbol.intern(null, (String)"datom")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        excise__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.excise__init").getClassLoader());
        try {
            excise__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


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
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.fulltext_index$datum__GT_doc;
import datomic.fulltext_index$fn__12323;
import datomic.fulltext_index$fn__12328;
import datomic.fulltext_index$fn__12331;
import datomic.fulltext_index$fn__12345;
import datomic.fulltext_index$loading__6434__auto____12224;
import datomic.fulltext_index$update_fulltext;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class fulltext_index__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final Keyword const__2;
    public static final AFn const__4;
    public static final AFn const__5;
    public static final Var const__6;
    public static final AFn const__11;
    public static final Object const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final ISeq const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final AFn const__23;
    public static final Keyword const__24;
    public static final AFn const__25;
    public static final Keyword const__26;
    public static final Keyword const__27;
    public static final AFn const__29;
    public static final Keyword const__30;
    public static final Var const__31;
    public static final Var const__32;
    public static final Var const__33;
    public static final AFn const__34;
    public static final AFn const__35;
    public static final Keyword const__36;
    public static final Var const__37;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__41;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__4);
        Object object2 = ((IFn)new fulltext_index$loading__6434__auto____12224()).invoke();
        if (((Symbol)const__1).equals((Object)const__5)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new fulltext_index$fn__12323())));
            v3 = null;
        }
        Var var = const__6;
        var.setMeta((IPersistentMap)const__11);
        Var var2 = var;
        var.bindRoot((Object)new fulltext_index$datum__GT_doc());
        Object object3 = ((IFn)new fulltext_index$fn__12328()).invoke();
        Object object4 = const__12;
        Object object5 = ((IFn)const__13.getRawRoot()).invoke((Object)const__14, const__15.getRawRoot(), (Object)const__2, null);
        Object object6 = ((IFn)const__16).invoke((Object)const__14, (Object)const__17);
        Object object7 = ((IFn)const__18.getRawRoot()).invoke((Object)const__14, const__19.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke((Object)const__23, (Object)const__24, (Object)const__25, (Object)const__26, (Object)const__14, (Object)const__27, (Object)const__29, (Object)const__30, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__31.getRawRoot()).invoke(const__32.get(), ((IFn)const__33.getRawRoot()).invoke((Object)const__34, ((IFn)const__19.getRawRoot()).invoke((Object)const__35, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__36, const__14})))), new fulltext_index$fn__12331()})));
        Object object8 = ((IFn)const__37.getRawRoot()).invoke(const__14.getRawRoot());
        AFn aFn = const__38;
        Object object9 = ((IFn)new fulltext_index$fn__12345()).invoke();
        Var var3 = const__39;
        var3.setMeta((IPersistentMap)const__41);
        Var var4 = var3;
        var3.bindRoot((Object)new fulltext_index$update_fulltext());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.fulltext-index")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"author"), "Stu Halloway"}));
        const__2 = RT.keyword(null, (String)"doc");
        const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "building fulltext index", RT.keyword(null, (String)"author"), "Stu Halloway"});
        const__5 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__6 = RT.var((String)"datomic.fulltext-index", (String)"datum->doc");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"datum")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDatum")}))))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.classForName((String)"datomic.fulltext_index.LuceneProvider");
        const__13 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__14 = RT.var((String)"datomic.fulltext-index", (String)"LuceneProvider");
        const__15 = RT.var((String)"clojure.core", (String)"assoc");
        const__16 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__17 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"fulltext-attr-reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"this"), (Object)Symbol.intern(null, (String)"attr"))))}))));
        const__18 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__19 = RT.var((String)"clojure.core", (String)"merge");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.fulltext_index.LuceneProvider"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.fulltext_index.LuceneProvider")});
        const__24 = RT.keyword(null, (String)"sigs");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"fulltext-attr-reader"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"fulltext-attr-reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"this"), (Object)Symbol.intern(null, (String)"attr"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"this"), (Object)Symbol.intern(null, (String)"attr")))), RT.keyword(null, (String)"doc"), null})});
        const__26 = RT.keyword(null, (String)"var");
        const__27 = RT.keyword(null, (String)"method-map");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"fulltext-attr-reader"), RT.keyword(null, (String)"fulltext-attr-reader")});
        const__30 = RT.keyword(null, (String)"method-builders");
        const__31 = RT.var((String)"clojure.core", (String)"intern");
        const__32 = RT.var((String)"clojure.core", (String)"*ns*");
        const__33 = RT.var((String)"clojure.core", (String)"with-meta");
        const__34 = (AFn)((IObj)Symbol.intern(null, (String)"fulltext-attr-reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"this"), (Object)Symbol.intern(null, (String)"attr"))))}));
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"fulltext-attr-reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"this"), (Object)Symbol.intern(null, (String)"attr"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"this"), (Object)Symbol.intern(null, (String)"attr")))), RT.keyword(null, (String)"doc"), null});
        const__36 = RT.keyword(null, (String)"protocol");
        const__37 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__38 = (AFn)Symbol.intern(null, (String)"LuceneProvider");
        const__39 = RT.var((String)"datomic.fulltext-index", (String)"update-fulltext");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"pft"), (Object)Symbol.intern(null, (String)"data")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        fulltext_index__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.fulltext_index__init").getClassLoader());
        try {
            fulltext_index__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


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
package datomic.core2.atom;

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
import datomic.core2.atom.logged$append_value;
import datomic.core2.atom.logged$create;
import datomic.core2.atom.logged$create_STAR_;
import datomic.core2.atom.logged$fn__19680;
import datomic.core2.atom.logged$fn__19686;
import datomic.core2.atom.logged$fn__19689;
import datomic.core2.atom.logged$fn__19702;
import datomic.core2.atom.logged$fn__19778;
import datomic.core2.atom.logged$load;
import datomic.core2.atom.logged$loading__6789__auto____19678;
import datomic.core2.atom.logged$next_header;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class logged__init {
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
    public static final AFn const__22;
    public static final Keyword const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final AFn const__27;
    public static final AFn const__28;
    public static final Keyword const__29;
    public static final AFn const__30;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__33;
    public static final Var const__34;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__52;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new logged$loading__6789__auto____19678()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new logged$fn__19680())));
            v2 = null;
        }
        Object object3 = ((IFn)new logged$fn__19686()).invoke();
        Object object4 = const__3;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, const__6.getRawRoot(), (Object)const__7, null);
        Object object6 = ((IFn)const__8).invoke((Object)const__5, (Object)const__9);
        Object object7 = ((IFn)const__10.getRawRoot()).invoke((Object)const__5, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__5, (Object)const__19, (Object)const__22, (Object)const__23, (Object)RT.map((Object[])new Object[]{((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__27, ((IFn)const__11.getRawRoot()).invoke((Object)const__28, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__5})))), new logged$fn__19689(), ((IFn)const__24.getRawRoot()).invoke(const__25.get(), ((IFn)const__26.getRawRoot()).invoke((Object)const__30, ((IFn)const__11.getRawRoot()).invoke((Object)const__31, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__29, const__5})))), new logged$fn__19702()})));
        Object object8 = ((IFn)const__32.getRawRoot()).invoke(const__5.getRawRoot());
        AFn aFn = const__33;
        Var var = const__34;
        var.setMeta((IPersistentMap)const__40);
        Var var2 = var;
        var.bindRoot((Object)new logged$next_header());
        Var var3 = const__41;
        var3.setMeta((IPersistentMap)const__43);
        Var var4 = var3;
        var3.bindRoot((Object)new logged$append_value());
        Object object9 = ((IFn)new logged$fn__19778()).invoke();
        Var var5 = const__44;
        var5.setMeta((IPersistentMap)const__46);
        Var var6 = var5;
        var5.bindRoot((Object)new logged$create_STAR_());
        Var var7 = const__47;
        var7.setMeta((IPersistentMap)const__49);
        Var var8 = var7;
        var7.bindRoot((Object)new logged$create());
        Var var9 = const__50;
        var9.setMeta((IPersistentMap)const__52);
        Var var10 = var9;
        var9.bindRoot((Object)new logged$load());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.atom.logged");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.classForName((String)"datomic.core2.atom.logged.LoggedAtomImpl");
        const__4 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__5 = RT.var((String)"datomic.core2.atom.logged", (String)"LoggedAtomImpl");
        const__6 = RT.var((String)"clojure.core", (String)"assoc");
        const__7 = RT.keyword(null, (String)"doc");
        const__8 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__9 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"-validated-v")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"v"))))})), ((IObj)Symbol.intern(null, (String)"-read-latest")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__10 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__11 = RT.var((String)"clojure.core", (String)"merge");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.core2.atom.logged.LoggedAtomImpl"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.core2.atom.logged.LoggedAtomImpl")});
        const__16 = RT.keyword(null, (String)"sigs");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-validated-v"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-validated-v")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"v"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"doc"), "Returns v or an anomaly."}), RT.keyword(null, (String)"-read-latest"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-read-latest")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns channel with latest value or anomaly."})});
        const__18 = RT.keyword(null, (String)"var");
        const__19 = RT.keyword(null, (String)"method-map");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"-read-latest"), RT.keyword(null, (String)"-read-latest"), RT.keyword(null, (String)"-validated-v"), RT.keyword(null, (String)"-validated-v")});
        const__23 = RT.keyword(null, (String)"method-builders");
        const__24 = RT.var((String)"clojure.core", (String)"intern");
        const__25 = RT.var((String)"clojure.core", (String)"*ns*");
        const__26 = RT.var((String)"clojure.core", (String)"with-meta");
        const__27 = (AFn)((IObj)Symbol.intern(null, (String)"-validated-v")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"v"))))}));
        const__28 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-validated-v")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"v"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"doc"), "Returns v or an anomaly."});
        const__29 = RT.keyword(null, (String)"protocol");
        const__30 = (AFn)((IObj)Symbol.intern(null, (String)"-read-latest")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), null, RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"-read-latest")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Returns channel with latest value or anomaly."});
        const__32 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__33 = (AFn)Symbol.intern(null, (String)"LoggedAtomImpl");
        const__34 = RT.var((String)"datomic.core2.atom.logged", (String)"next-header");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"tombstone")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"header")})))), RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.core2.atom.logged", (String)"append-value");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"log"), (Object)Symbol.intern(null, (String)"serialize"), (Object)Symbol.intern(null, (String)"header"), (Object)Symbol.intern(null, (String)"value")))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.core2.atom.logged", (String)"create*");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"log"), (Object)Symbol.intern(null, (String)"serialize"), (Object)Symbol.intern(null, (String)"deserialize"), (Object)Symbol.intern(null, (String)"refresh-msec"), (Object)Symbol.intern(null, (String)"validator"))}), (Object)Symbol.intern(null, (String)"state")))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.core2.atom.logged", (String)"create");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"log"), (Object)Symbol.intern(null, (String)"header"), (Object)Symbol.intern(null, (String)"value"), (Object)Symbol.intern(null, (String)"serialize")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"args")})))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.core2.atom.logged", (String)"load");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        logged__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.atom.logged__init").getClassLoader());
        try {
            logged__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


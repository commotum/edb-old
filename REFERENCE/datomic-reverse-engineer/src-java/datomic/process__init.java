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
import datomic.process$claim_pid_file;
import datomic.process$create_instance;
import datomic.process$fail_on_exception;
import datomic.process$fn__14885;
import datomic.process$fn__14893;
import datomic.process$fn__14896;
import datomic.process$fn__14917;
import datomic.process$fn__14928;
import datomic.process$fn__14942;
import datomic.process$fn__14985;
import datomic.process$loading__6434__auto____14883;
import datomic.process$throw_if_failing_BANG_;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class process__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Object const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Var const__9;
    public static final ISeq const__10;
    public static final Var const__11;
    public static final Var const__12;
    public static final AFn const__16;
    public static final Keyword const__17;
    public static final AFn const__18;
    public static final Keyword const__19;
    public static final Keyword const__20;
    public static final AFn const__24;
    public static final Keyword const__25;
    public static final Var const__26;
    public static final Var const__27;
    public static final Var const__28;
    public static final AFn const__29;
    public static final AFn const__30;
    public static final Keyword const__31;
    public static final AFn const__32;
    public static final AFn const__33;
    public static final AFn const__34;
    public static final AFn const__35;
    public static final Var const__36;
    public static final AFn const__37;
    public static final Var const__38;
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
        Object object2 = ((IFn)new process$loading__6434__auto____14883()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new process$fn__14885())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new process$fn__14893()).invoke();
        Object object5 = const__4;
        Object object6 = ((IFn)const__5.getRawRoot()).invoke((Object)const__6, const__7.getRawRoot(), (Object)const__8, null);
        Object object7 = ((IFn)const__9).invoke((Object)const__6, (Object)const__10);
        Object object8 = ((IFn)const__11.getRawRoot()).invoke((Object)const__6, const__12.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__16, (Object)const__17, (Object)const__18, (Object)const__19, (Object)const__6, (Object)const__20, (Object)const__24, (Object)const__25, (Object)RT.map((Object[])new Object[]{((IFn)const__26.getRawRoot()).invoke(const__27.get(), ((IFn)const__28.getRawRoot()).invoke((Object)const__29, ((IFn)const__12.getRawRoot()).invoke((Object)const__30, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__31, const__6})))), new process$fn__14896(), ((IFn)const__26.getRawRoot()).invoke(const__27.get(), ((IFn)const__28.getRawRoot()).invoke((Object)const__32, ((IFn)const__12.getRawRoot()).invoke((Object)const__33, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__31, const__6})))), new process$fn__14917(), ((IFn)const__26.getRawRoot()).invoke(const__27.get(), ((IFn)const__28.getRawRoot()).invoke((Object)const__34, ((IFn)const__12.getRawRoot()).invoke((Object)const__35, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__31, const__6})))), new process$fn__14928()})));
        Object object9 = ((IFn)const__36.getRawRoot()).invoke(const__6.getRawRoot());
        AFn aFn = const__37;
        Object object10 = ((IFn)new process$fn__14942()).invoke();
        Var var = const__38;
        var.setMeta((IPersistentMap)const__43);
        Var var2 = var;
        var.bindRoot((Object)new process$create_instance());
        Object object11 = ((IFn)new process$fn__14985()).invoke();
        Var var3 = const__44;
        var3.setMeta((IPersistentMap)const__46);
        Var var4 = var3;
        var3.bindRoot((Object)new process$claim_pid_file());
        Var var5 = const__47;
        var5.setMeta((IPersistentMap)const__49);
        Var var6 = var5;
        var5.bindRoot((Object)new process$throw_if_failing_BANG_());
        Var var7 = const__50;
        var7.setMeta((IPersistentMap)const__52);
        Var var8 = var7;
        var7.bindRoot((Object)new process$fail_on_exception());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.process");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.classForName((String)"datomic.process.CriticalFailure");
        const__5 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__6 = RT.var((String)"datomic.process", (String)"CriticalFailure");
        const__7 = RT.var((String)"clojure.core", (String)"assoc");
        const__8 = RT.keyword(null, (String)"doc");
        const__9 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__10 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"add-fail-handler")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"h"))))})), ((IObj)Symbol.intern(null, (String)"failing?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), ((IObj)Symbol.intern(null, (String)"fail")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"t"))))}))));
        const__11 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__12 = RT.var((String)"clojure.core", (String)"merge");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.process.CriticalFailure"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.process.CriticalFailure")});
        const__17 = RT.keyword(null, (String)"sigs");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"add-fail-handler"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"add-fail-handler")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"h"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"h")))), RT.keyword(null, (String)"doc"), "Add a function to be called in even of a critical process failure.\n   Critical failure handlers should be idempotent, e.g. via a delay."}), RT.keyword(null, (String)"failing?"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"failing?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Is process in a critical failure? Once true can never be false."}), RT.keyword(null, (String)"fail"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"fail")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"t"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"t")))), RT.keyword(null, (String)"doc"), "Fail the process."})});
        const__19 = RT.keyword(null, (String)"var");
        const__20 = RT.keyword(null, (String)"method-map");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"failing?"), RT.keyword(null, (String)"failing?"), RT.keyword(null, (String)"fail"), RT.keyword(null, (String)"fail"), RT.keyword(null, (String)"add-fail-handler"), RT.keyword(null, (String)"add-fail-handler")});
        const__25 = RT.keyword(null, (String)"method-builders");
        const__26 = RT.var((String)"clojure.core", (String)"intern");
        const__27 = RT.var((String)"clojure.core", (String)"*ns*");
        const__28 = RT.var((String)"clojure.core", (String)"with-meta");
        const__29 = (AFn)((IObj)Symbol.intern(null, (String)"fail")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"t"))))}));
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"fail")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"t"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg")), Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"msg"), (Object)Symbol.intern(null, (String)"t")))), RT.keyword(null, (String)"doc"), "Fail the process."});
        const__31 = RT.keyword(null, (String)"protocol");
        const__32 = (AFn)((IObj)Symbol.intern(null, (String)"failing?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"failing?")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Is process in a critical failure? Once true can never be false."});
        const__34 = (AFn)((IObj)Symbol.intern(null, (String)"add-fail-handler")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"h"))))}));
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"add-fail-handler")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"h"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"), (Object)Symbol.intern(null, (String)"h")))), RT.keyword(null, (String)"doc"), "Add a function to be called in even of a critical process failure.\n   Critical failure handlers should be idempotent, e.g. via a delay."});
        const__36 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__37 = (AFn)Symbol.intern(null, (String)"CriticalFailure");
        const__38 = RT.var((String)"datomic.process", (String)"create-instance");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"shutdown-time"), (Object)Symbol.intern(null, (String)"exit?")))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.process", (String)"claim-pid-file");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.process", (String)"throw-if-failing!");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.process", (String)"fail-on-exception");
        const__52 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        process__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.process__init").getClassLoader());
        try {
            process__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


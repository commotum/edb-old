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
import datomic.future$_future_with_channel_impl;
import datomic.future$add_bounding_warning;
import datomic.future$filling_promise;
import datomic.future$fn__10143;
import datomic.future$fn__10150;
import datomic.future$fn__10153;
import datomic.future$fn__10164;
import datomic.future$fn__10169;
import datomic.future$future;
import datomic.future$future_call;
import datomic.future$loading__6434__auto____10141;
import datomic.future$pfuture;
import datomic.future$pfuture_call;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class future__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__10;
    public static final Object const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Keyword const__15;
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
    public static final Var const__42;
    public static final AFn const__44;
    public static final Var const__45;
    public static final AFn const__46;
    public static final Var const__47;
    public static final Object const__48;
    public static final Var const__49;
    public static final AFn const__51;
    public static final Var const__52;
    public static final AFn const__54;
    public static final Var const__55;
    public static final AFn const__57;
    public static final Var const__58;
    public static final AFn const__60;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new future$loading__6434__auto____10141()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new future$fn__10143())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)new future$filling_promise());
        Object object4 = ((IFn)new future$fn__10150()).invoke();
        Object object5 = const__11;
        Object object6 = ((IFn)const__12.getRawRoot()).invoke((Object)const__13, const__14.getRawRoot(), (Object)const__15, null);
        Object object7 = ((IFn)const__16).invoke((Object)const__13, (Object)const__17);
        Object object8 = ((IFn)const__18.getRawRoot()).invoke((Object)const__13, const__19.getRawRoot(), ((IFn)const__14.getRawRoot()).invoke((Object)const__23, (Object)const__24, (Object)const__25, (Object)const__26, (Object)const__13, (Object)const__27, (Object)const__29, (Object)const__30, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__31.getRawRoot()).invoke(const__32.get(), ((IFn)const__33.getRawRoot()).invoke((Object)const__34, ((IFn)const__19.getRawRoot()).invoke((Object)const__35, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__36, const__13})))), new future$fn__10153()})));
        Object object9 = ((IFn)const__37.getRawRoot()).invoke(const__13.getRawRoot());
        AFn aFn = const__38;
        Object object10 = ((IFn)new future$fn__10164()).invoke();
        Object object11 = ((IFn)new future$fn__10169()).invoke();
        Var var3 = const__39;
        var3.setMeta((IPersistentMap)const__41);
        Var var4 = var3;
        var3.bindRoot((Object)new future$_future_with_channel_impl());
        Var var5 = const__42;
        var5.setMeta((IPersistentMap)const__44);
        Var var6 = var5;
        var5.bindRoot((Object)new future$add_bounding_warning());
        Var var7 = const__45;
        var7.setMeta((IPersistentMap)const__46);
        Var var8 = var7;
        var7.bindRoot(((IFn)const__47.getRawRoot()).invoke(const__48));
        Var var9 = const__49;
        var9.setMeta((IPersistentMap)const__51);
        Var var10 = var9;
        var9.bindRoot((Object)new future$future());
        const__49.setMacro();
        Object v23 = null;
        Var var11 = const__49;
        Var var12 = const__52;
        var12.setMeta((IPersistentMap)const__54);
        Var var13 = var12;
        var12.bindRoot((Object)new future$future_call());
        const__52.setMacro();
        Object v27 = null;
        Var var14 = const__52;
        Var var15 = const__55;
        var15.setMeta((IPersistentMap)const__57);
        Var var16 = var15;
        var15.bindRoot((Object)new future$pfuture());
        const__55.setMacro();
        Object v31 = null;
        Var var17 = const__55;
        Var var18 = const__58;
        var18.setMeta((IPersistentMap)const__60);
        Var var19 = var18;
        var18.bindRoot((Object)new future$pfuture_call());
        const__58.setMacro();
        Object v35 = null;
        Var var20 = const__58;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.future");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.future", (String)"filling-promise");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.classForName((String)"datomic.future.GetChannel");
        const__12 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__13 = RT.var((String)"datomic.future", (String)"GetChannel");
        const__14 = RT.var((String)"clojure.core", (String)"assoc");
        const__15 = RT.keyword(null, (String)"doc");
        const__16 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__17 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"get-channel")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"fut"))))}))));
        const__18 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__19 = RT.var((String)"clojure.core", (String)"merge");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.future.GetChannel"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.future.GetChannel")});
        const__24 = RT.keyword(null, (String)"sigs");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"get-channel"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"get-channel")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"fut"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"fut")))), RT.keyword(null, (String)"doc"), "Returns a promise channel of return value (or exception) from fut.\nIf future returns nil, channel will get the special value ::nil."})});
        const__26 = RT.keyword(null, (String)"var");
        const__27 = RT.keyword(null, (String)"method-map");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"get-channel"), RT.keyword(null, (String)"get-channel")});
        const__30 = RT.keyword(null, (String)"method-builders");
        const__31 = RT.var((String)"clojure.core", (String)"intern");
        const__32 = RT.var((String)"clojure.core", (String)"*ns*");
        const__33 = RT.var((String)"clojure.core", (String)"with-meta");
        const__34 = (AFn)((IObj)Symbol.intern(null, (String)"get-channel")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"fut"))))}));
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"get-channel")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"fut"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"fut")))), RT.keyword(null, (String)"doc"), "Returns a promise channel of return value (or exception) from fut.\nIf future returns nil, channel will get the special value ::nil."});
        const__36 = RT.keyword(null, (String)"protocol");
        const__37 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__38 = (AFn)Symbol.intern(null, (String)"GetChannel");
        const__39 = RT.var((String)"datomic.future", (String)"-future-with-channel-impl");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"exec")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ExecutorService")})), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__42 = RT.var((String)"datomic.future", (String)"add-bounding-warning");
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"promise-ch"), (Object)Symbol.intern(null, (String)"context"), (Object)Symbol.intern(null, (String)"seconds")))), RT.keyword(null, (String)"column"), 1});
        const__45 = RT.var((String)"datomic.future", (String)"bounding-warn-seconds");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"clojure.core", (String)"atom");
        const__48 = 180L;
        const__49 = RT.var((String)"datomic.future", (String)"future");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.future", (String)"future-call");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.future", (String)"pfuture");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"exec"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.future", (String)"pfuture-call");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"exec"), (Object)Symbol.intern(null, (String)"f")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        future__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.future__init").getClassLoader());
        try {
            future__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


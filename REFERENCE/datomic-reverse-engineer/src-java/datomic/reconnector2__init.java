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
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.reconnector2$fn__17110;
import datomic.reconnector2$fn__17114;
import datomic.reconnector2$fn__17117;
import datomic.reconnector2$fn__17128;
import datomic.reconnector2$fn__17144;
import datomic.reconnector2$loading__6434__auto____17108;
import datomic.reconnector2$reconnector_ref;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class reconnector2__init {
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
    public static final Var const__31;
    public static final AFn const__35;
    public static final Var const__36;
    public static final Object const__37;
    public static final Var const__38;
    public static final AFn const__41;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new reconnector2$loading__6434__auto____17108()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new reconnector2$fn__17110())));
            v2 = null;
        }
        Object object3 = ((IFn)new reconnector2$fn__17114()).invoke();
        Object object4 = const__3;
        Object object5 = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, const__6.getRawRoot(), (Object)const__7, null);
        Object object6 = ((IFn)const__8).invoke((Object)const__5, (Object)const__9);
        Object object7 = ((IFn)const__10.getRawRoot()).invoke((Object)const__5, const__11.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke((Object)const__15, (Object)const__16, (Object)const__17, (Object)const__18, (Object)const__5, (Object)const__19, (Object)const__21, (Object)const__22, (Object)RT.mapUniqueKeys((Object[])new Object[]{((IFn)const__23.getRawRoot()).invoke(const__24.get(), ((IFn)const__25.getRawRoot()).invoke((Object)const__26, ((IFn)const__11.getRawRoot()).invoke((Object)const__27, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__28, const__5})))), new reconnector2$fn__17117()})));
        Object object8 = ((IFn)const__29.getRawRoot()).invoke(const__5.getRawRoot());
        AFn aFn = const__30;
        Var var = const__31;
        Var var2 = var;
        var.setMeta((IPersistentMap)const__35);
        Object object9 = ((IFn)new reconnector2$fn__17128()).invoke();
        MultiFn multiFn = ((MultiFn)const__36.getRawRoot()).addMethod(const__37, (IFn)new reconnector2$fn__17144());
        Var var3 = const__38;
        var3.setMeta((IPersistentMap)const__41);
        Var var4 = var3;
        var3.bindRoot((Object)new reconnector2$reconnector_ref());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.reconnector2");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.classForName((String)"datomic.reconnector2.Reconnectable");
        const__4 = RT.var((String)"clojure.core", (String)"alter-meta!");
        const__5 = RT.var((String)"datomic.reconnector2", (String)"Reconnectable");
        const__6 = RT.var((String)"clojure.core", (String)"assoc");
        const__7 = RT.keyword(null, (String)"doc");
        const__8 = RT.var((String)"clojure.core", (String)"assert-same-protocol");
        const__9 = (ISeq)PersistentList.create(Arrays.asList(((IObj)Symbol.intern(null, (String)"reconnect")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}))));
        const__10 = RT.var((String)"clojure.core", (String)"alter-var-root");
        const__11 = RT.var((String)"clojure.core", (String)"merge");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"on"), Symbol.intern(null, (String)"datomic.reconnector2.Reconnectable"), RT.keyword(null, (String)"on-interface"), RT.classForName((String)"datomic.reconnector2.Reconnectable")});
        const__16 = RT.keyword(null, (String)"sigs");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"reconnect"), RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"reconnect")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Try to reconnect. Idempotent. Async. Calls cleanon on previous state Returns ok."})});
        const__18 = RT.keyword(null, (String)"var");
        const__19 = RT.keyword(null, (String)"method-map");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"reconnect"), RT.keyword(null, (String)"reconnect")});
        const__22 = RT.keyword(null, (String)"method-builders");
        const__23 = RT.var((String)"clojure.core", (String)"intern");
        const__24 = RT.var((String)"clojure.core", (String)"*ns*");
        const__25 = RT.var((String)"clojure.core", (String)"with-meta");
        const__26 = (AFn)((IObj)Symbol.intern(null, (String)"reconnect")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))}));
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"name"), ((IObj)Symbol.intern(null, (String)"reconnect")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_"))))})), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"_")))), RT.keyword(null, (String)"doc"), "Try to reconnect. Idempotent. Async. Calls cleanon on previous state Returns ok."});
        const__28 = RT.keyword(null, (String)"protocol");
        const__29 = RT.var((String)"clojure.core", (String)"-reset-methods");
        const__30 = (AFn)Symbol.intern(null, (String)"Reconnectable");
        const__31 = RT.var((String)"datomic.reconnector2", (String)"shutdown?");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"declared"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__36 = RT.var((String)"clojure.core", (String)"print-method");
        const__37 = RT.classForName((String)"datomic.reconnector2.Reconnector");
        const__38 = RT.var((String)"datomic.reconnector2", (String)"reconnector-ref");
        const__41 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"state"), (Object)Symbol.intern(null, (String)"reconnect"), (Object)Symbol.intern(null, (String)"cleanup"), (Object)Symbol.intern(null, (String)"shutdown-state"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        reconnector2__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.reconnector2__init").getClassLoader());
        try {
            reconnector2__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


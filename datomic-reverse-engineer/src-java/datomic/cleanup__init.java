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
import datomic.cleanup$create_manager;
import datomic.cleanup$fn__20737;
import datomic.cleanup$fn__20739;
import datomic.cleanup$fn__20749;
import datomic.cleanup$loading__6434__auto____20735;
import datomic.cleanup$register_cleanup;
import datomic.cleanup$run_queue_loop;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class cleanup__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__8;
    public static final Var const__9;
    public static final AFn const__11;
    public static final Var const__12;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__16;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new cleanup$loading__6434__auto____20735()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new cleanup$fn__20737())));
            v2 = null;
        }
        Object object3 = ((IFn)new cleanup$fn__20739()).invoke();
        Var var = const__3;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)new cleanup$register_cleanup());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new cleanup$create_manager());
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot((Object)new cleanup$run_queue_loop());
        Var var7 = const__15;
        var7.setMeta((IPersistentMap)const__16);
        Var var8 = var7;
        var7.bindRoot((Object)new Delay((IFn)new cleanup$fn__20749()));
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cleanup");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.cleanup", (String)"register-cleanup");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"manager")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Manager")})), (Object)Symbol.intern(null, (String)"object"), (Object)Symbol.intern(null, (String)"cleanup")))), RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.cleanup", (String)"create-manager");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create())), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.cleanup", (String)"run-queue-loop");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"q"), (Object)Symbol.intern(null, (String)"error-handler")))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.cleanup", (String)"shared-manager-ref");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    }

    static {
        cleanup__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cleanup__init").getClassLoader());
        try {
            cleanup__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


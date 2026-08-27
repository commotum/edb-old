/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.core2.val_store.double_store$create;
import datomic.core2.val_store.double_store$fn__21080;
import datomic.core2.val_store.double_store$fn__21086;
import datomic.core2.val_store.double_store$get_from_near_store_QMARK_;
import datomic.core2.val_store.double_store$loading__6789__auto____21078;
import datomic.core2.val_store.double_store$read_repair_near_store_QMARK_;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class double_store__init {
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

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new double_store$loading__6789__auto____21078()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new double_store$fn__21080())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)new double_store$get_from_near_store_QMARK_());
        Var var3 = const__11;
        var3.setMeta((IPersistentMap)const__13);
        Var var4 = var3;
        var3.bindRoot((Object)new double_store$read_repair_near_store_QMARK_());
        Object object4 = ((IFn)new double_store$fn__21086()).invoke();
        Var var5 = const__14;
        var5.setMeta((IPersistentMap)const__16);
        Var var6 = var5;
        var5.bindRoot((Object)new double_store$create());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.val-store.double-store");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.core2.val-store.double-store", (String)"get-from-near-store?");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.core2.val-store.double-store", (String)"read-repair-near-store?");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"column"), 1});
        const__14 = RT.var((String)"datomic.core2.val-store.double-store", (String)"create");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"near-store"), (Object)Symbol.intern(null, (String)"far-store"), (Object)Symbol.intern(null, (String)"repair-metric"), (Object)Symbol.intern(null, (String)"get-fallback-msec")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"get-fallback-msec"), 20L, Symbol.intern(null, (String)"repair-metric"), RT.keyword(null, (String)"fs.repair")})})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        double_store__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.val_store.double_store__init").getClassLoader());
        try {
            double_store__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


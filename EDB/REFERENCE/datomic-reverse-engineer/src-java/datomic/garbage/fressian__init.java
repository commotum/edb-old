/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
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
package datomic.garbage;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.garbage.fressian$fn__16568;
import datomic.garbage.fressian$fn__16571;
import datomic.garbage.fressian$fn__16592;
import datomic.garbage.fressian$fn__16613;
import datomic.garbage.fressian$loading__6434__auto____16566;
import datomic.garbage.fressian$read_handler;
import datomic.garbage.fressian$reify__16639;
import datomic.garbage.fressian$reify__16641;
import datomic.garbage.fressian$write_handler;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class fressian__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Object const__17;
    public static final Object const__18;
    public static final Object const__19;
    public static final Object const__20;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__26;
    public static final Var const__27;
    public static final Var const__28;
    public static final Var const__29;
    public static final Var const__30;
    public static final AFn const__33;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new fressian$loading__6434__auto____16566()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new fressian$fn__16568())));
            v2 = null;
        }
        Object object3 = ((IFn)new fressian$fn__16571()).invoke();
        Object object4 = ((IFn)new fressian$fn__16592()).invoke();
        Object object5 = ((IFn)new fressian$fn__16613()).invoke();
        Var var = const__3;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new fressian$write_handler());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new fressian$read_handler());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot(((IFn)const__15.getRawRoot()).invoke(const__16.getRawRoot(), const__17, (Object)RT.mapUniqueKeys((Object[])new Object[]{"garbage-root", ((IFn)const__3.getRawRoot()).invoke((Object)"garbage-root")}), const__18, (Object)RT.mapUniqueKeys((Object[])new Object[]{"garbage-dir", ((IFn)const__3.getRawRoot()).invoke((Object)"garbage-dir")}), const__19, (Object)RT.mapUniqueKeys((Object[])new Object[]{"garbage-leaf", ((IFn)const__3.getRawRoot()).invoke((Object)"garbage-leaf")}), const__20, (Object)RT.mapUniqueKeys((Object[])new Object[]{"vec", ((IObj)new fressian$reify__16639(null)).withMeta((IPersistentMap)const__24)})));
        Var var7 = const__25;
        var7.setMeta((IPersistentMap)const__26);
        Var var8 = var7;
        var7.bindRoot(((IFn)const__15.getRawRoot()).invoke(const__27.getRawRoot(), (Object)"garbage-root", ((IFn)const__10.getRawRoot()).invoke(const__28.getRawRoot()), (Object)"garbage-dir", ((IFn)const__10.getRawRoot()).invoke(const__29.getRawRoot()), (Object)"garbage-leaf", ((IFn)const__10.getRawRoot()).invoke(const__30.getRawRoot()), (Object)"vec", (Object)((IObj)new fressian$reify__16641(null)).withMeta((IPersistentMap)const__33)));
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.garbage.fressian");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.garbage.fressian", (String)"write-handler");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"tag")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.garbage.fressian", (String)"read-handler");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"factory")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.garbage.fressian", (String)"write-handlers");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"clojure.core", (String)"assoc");
        const__16 = RT.var((String)"datomic.fressian", (String)"user-write-handlers");
        const__17 = RT.classForName((String)"datomic.garbage.fressian.GarbageRoot");
        const__18 = RT.classForName((String)"datomic.garbage.fressian.GarbageDir");
        const__19 = RT.classForName((String)"datomic.garbage.fressian.GarbageLeaf");
        const__20 = RT.classForName((String)"clojure.lang.PersistentVector");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 39, RT.keyword(null, (String)"column"), 12});
        const__25 = RT.var((String)"datomic.garbage.fressian", (String)"read-handlers");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.fressian", (String)"user-read-handlers");
        const__28 = RT.var((String)"datomic.garbage.fressian", (String)"->GarbageRoot");
        const__29 = RT.var((String)"datomic.garbage.fressian", (String)"->GarbageDir");
        const__30 = RT.var((String)"datomic.garbage.fressian", (String)"->GarbageLeaf");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 53, RT.keyword(null, (String)"column"), 5});
    }

    static {
        fressian__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.garbage.fressian__init").getClassLoader());
        try {
            fressian__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


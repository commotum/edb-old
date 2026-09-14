/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.PersistentArrayMap
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
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.kv_hotrod$fn__17794;
import datomic.kv_hotrod$fn__17796;
import datomic.kv_hotrod$fn__17799;
import datomic.kv_hotrod$fn__17801;
import datomic.kv_hotrod$kv_infinispan;
import datomic.kv_hotrod$loading__6434__auto____17792;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class kv_hotrod__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__7;
    public static final Var const__8;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__11;
    public static final Var const__12;
    public static final Var const__13;
    public static final AFn const__16;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new kv_hotrod$loading__6434__auto____17792()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new kv_hotrod$fn__17794())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot((Object)new Delay((IFn)new kv_hotrod$fn__17796()));
        Var var3 = const__8;
        var3.setMeta((IPersistentMap)const__9);
        Var var4 = var3;
        var3.bindRoot(((IFn)new kv_hotrod$fn__17799()).invoke());
        Var var5 = const__10;
        var5.setMeta((IPersistentMap)const__11);
        Var var6 = var5;
        var5.bindRoot(((IFn)const__12.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY));
        Object object4 = ((IFn)new kv_hotrod$fn__17801()).invoke();
        Var var7 = const__13;
        var7.setMeta((IPersistentMap)const__16);
        Var var8 = var7;
        var7.bindRoot((Object)new kv_hotrod$kv_infinispan());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.kv-hotrod");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.kv-hotrod", (String)"FORCE");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__8 = RT.var((String)"datomic.kv-hotrod", (String)"PROPS");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.kv-hotrod", (String)"managers");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"clojure.core", (String)"atom");
        const__13 = RT.var((String)"datomic.kv-hotrod", (String)"kv-infinispan");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"endpoint")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        kv_hotrod__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.kv_hotrod__init").getClassLoader());
        try {
            kv_hotrod__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


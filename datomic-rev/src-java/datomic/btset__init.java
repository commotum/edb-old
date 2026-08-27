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
package datomic;

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
import datomic.btset$bench;
import datomic.btset$btset;
import datomic.btset$comp;
import datomic.btset$fn__11800;
import datomic.btset$fn__11803;
import datomic.btset$fn__11805;
import datomic.btset$fn__11807;
import datomic.btset$fn__11812;
import datomic.btset$fn__11814;
import datomic.btset$fn__11819;
import datomic.btset$fn__11821;
import datomic.btset$fn__11830;
import datomic.btset$fn__11837;
import datomic.btset$fn__11844;
import datomic.btset$fn__11846;
import datomic.btset$fn__11848;
import datomic.btset$loading__6434__auto____11721;
import datomic.btset$rseek;
import datomic.btset$seek;
import datomic.btset$seek_last;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class btset__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final AFn const__11;
    public static final Var const__12;
    public static final AFn const__14;
    public static final Object const__15;
    public static final Var const__16;
    public static final AFn const__17;
    public static final Var const__18;
    public static final AFn const__22;
    public static final Var const__23;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;
    public static final Var const__30;
    public static final AFn const__32;
    public static final Var const__33;
    public static final AFn const__35;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new btset$loading__6434__auto____11721()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new btset$fn__11800())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = const__4.set((Object)Boolean.TRUE);
        Var var = const__5;
        var.setMeta((IPersistentMap)const__11);
        Var var2 = var;
        var.bindRoot((Object)new btset$comp());
        Object object5 = ((IFn)new btset$fn__11803()).invoke();
        Object object6 = ((IFn)new btset$fn__11805()).invoke();
        Object object7 = ((IFn)new btset$fn__11807()).invoke();
        Object object8 = ((IFn)new btset$fn__11812()).invoke();
        Object object9 = ((IFn)new btset$fn__11814()).invoke();
        Object object10 = ((IFn)new btset$fn__11819()).invoke();
        Object object11 = ((IFn)new btset$fn__11821()).invoke();
        Var var3 = const__12;
        var3.setMeta((IPersistentMap)const__14);
        Var var4 = var3;
        var3.bindRoot(const__15);
        Var var5 = const__16;
        var5.setMeta((IPersistentMap)const__17);
        Var var6 = var5;
        var5.bindRoot(const__15);
        Object object12 = ((IFn)new btset$fn__11830()).invoke();
        Object object13 = ((IFn)new btset$fn__11837()).invoke();
        Object object14 = ((IFn)new btset$fn__11844()).invoke();
        Object object15 = ((IFn)new btset$fn__11846()).invoke();
        Object object16 = ((IFn)new btset$fn__11848()).invoke();
        Var var7 = const__18;
        var7.setMeta((IPersistentMap)const__22);
        Var var8 = var7;
        var7.bindRoot((Object)new btset$btset());
        Var var9 = const__23;
        var9.setMeta((IPersistentMap)const__26);
        Var var10 = var9;
        var9.bindRoot((Object)new btset$seek());
        Var var11 = const__27;
        var11.setMeta((IPersistentMap)const__29);
        Var var12 = var11;
        var11.bindRoot((Object)new btset$seek_last());
        Var var13 = const__30;
        var13.setMeta((IPersistentMap)const__32);
        Var var14 = var13;
        var13.bindRoot((Object)new btset$rseek());
        Var var15 = const__33;
        var15.setMeta((IPersistentMap)const__35);
        Var var16 = var15;
        var15.bindRoot((Object)new btset$bench());
        Object v33 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.btset");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"clojure.core", (String)"*unchecked-math*");
        const__5 = RT.var((String)"datomic.btset", (String)"comp");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cmp")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.Comparator")})), (Object)Symbol.intern(null, (String)"x"), (Object)Symbol.intern(null, (String)"y"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.btset", (String)"BT_BRANCH_SIZE");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__15 = 16L;
        const__16 = RT.var((String)"datomic.btset", (String)"BT_LEAF_SIZE");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.btset", (String)"btset");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.btset.BTSet"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create(), Tuple.create((Object)Symbol.intern(null, (String)"cmp")))), RT.keyword(null, (String)"column"), 1});
        const__23 = RT.var((String)"datomic.btset", (String)"seek");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.iter.Iter"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"ds")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDataSet")}))), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"ds")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDataSet")})), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.btset", (String)"seek-last");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.iter.Iter"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"ds")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IDataSet")}))))), RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"datomic.btset", (String)"rseek");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.iter.Iter"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"bt")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"BTSet")}))), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"bt")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"BTSet")})), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__33 = RT.var((String)"datomic.btset", (String)"bench");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        btset__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.btset__init").getClassLoader());
        try {
            btset__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


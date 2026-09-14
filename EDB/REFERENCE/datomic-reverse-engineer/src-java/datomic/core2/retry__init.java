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
package datomic.core2;

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
import datomic.core2.retry$calc_exp_backoff;
import datomic.core2.retry$exp;
import datomic.core2.retry$fn__20986;
import datomic.core2.retry$full_jitter;
import datomic.core2.retry$limiting_retry;
import datomic.core2.retry$linear;
import datomic.core2.retry$loading__6789__auto____20984;
import datomic.core2.retry$retry;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class retry__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;
    public static final Var const__13;
    public static final AFn const__15;
    public static final Var const__16;
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__21;
    public static final Var const__22;
    public static final AFn const__24;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new retry$loading__6789__auto____20984()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new retry$fn__20986())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new retry$retry());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new retry$limiting_retry());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new retry$linear());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new retry$calc_exp_backoff());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new retry$full_jitter());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new retry$exp());
        Object v16 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.retry");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.core2.retry", (String)"retry");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"pred"), (Object)Symbol.intern(null, (String)"retry?"), (Object)Symbol.intern(null, (String)"calc-backoff"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"on-success"), (Object)Symbol.intern(null, (String)"on-failure")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"on-success"), Symbol.intern(null, (String)"identity"), Symbol.intern(null, (String)"on-failure"), Symbol.intern(null, (String)"identity")})})))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.core2.retry", (String)"limiting-retry");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)RT.map((Object[])new Object[]{((IObj)Symbol.intern(null, (String)"i")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), RT.keyword(null, (String)"i")})))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.core2.retry", (String)"linear");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"pred"), (Object)Symbol.intern(null, (String)"iter"), (Object)Symbol.intern(null, (String)"backoff")), Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"pred"), (Object)Symbol.intern(null, (String)"iter"), (Object)Symbol.intern(null, (String)"backoff"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.core2.retry", (String)"calc-exp-backoff");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)((IObj)Symbol.intern(null, (String)"backoff")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)((IObj)Symbol.intern(null, (String)"base")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"i")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})))}))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.core2.retry", (String)"full-jitter");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"i")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.core2.retry", (String)"exp");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"pred"), (Object)Symbol.intern(null, (String)"iter"), (Object)Symbol.intern(null, (String)"backoff"), (Object)Symbol.intern(null, (String)"base")), Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"pred"), (Object)Symbol.intern(null, (String)"iter"), (Object)Symbol.intern(null, (String)"backoff"), (Object)Symbol.intern(null, (String)"base"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        retry__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.retry__init").getClassLoader());
        try {
            retry__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


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
import datomic.core2.anomalies$_slet_STAR_;
import datomic.core2.anomalies$anom;
import datomic.core2.anomalies$athrow;
import datomic.core2.anomalies$busy_QMARK_;
import datomic.core2.anomalies$conflict_QMARK_;
import datomic.core2.anomalies$fault;
import datomic.core2.anomalies$fault_QMARK_;
import datomic.core2.anomalies$fn__19441;
import datomic.core2.anomalies$forbidden_QMARK_;
import datomic.core2.anomalies$incorrect_QMARK_;
import datomic.core2.anomalies$loading__6789__auto____19439;
import datomic.core2.anomalies$not_found_QMARK_;
import datomic.core2.anomalies$ok_QMARK_;
import datomic.core2.anomalies$ok__GT_;
import datomic.core2.anomalies$returning;
import datomic.core2.anomalies$slet;
import datomic.core2.anomalies$unavailable_QMARK_;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class anomalies__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__7;
    public static final AFn const__10;
    public static final Var const__11;
    public static final AFn const__12;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__21;
    public static final Var const__22;
    public static final AFn const__24;
    public static final Var const__25;
    public static final AFn const__27;
    public static final Var const__28;
    public static final AFn const__30;
    public static final Var const__31;
    public static final AFn const__33;
    public static final Var const__34;
    public static final AFn const__36;
    public static final Var const__37;
    public static final AFn const__39;
    public static final Var const__40;
    public static final AFn const__42;
    public static final Var const__43;
    public static final AFn const__45;
    public static final Var const__46;
    public static final AFn const__48;
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
        Object object2 = ((IFn)new anomalies$loading__6789__auto____19439()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new anomalies$fn__19441())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot((Object)const__10);
        Var var3 = const__11;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)const__14);
        Var var5 = const__15;
        var5.setMeta((IPersistentMap)const__18);
        Var var6 = var5;
        var5.bindRoot((Object)new anomalies$anom());
        Var var7 = const__19;
        var7.setMeta((IPersistentMap)const__21);
        Var var8 = var7;
        var7.bindRoot((Object)new anomalies$ok_QMARK_());
        Var var9 = const__22;
        var9.setMeta((IPersistentMap)const__24);
        Var var10 = var9;
        var9.bindRoot((Object)new anomalies$forbidden_QMARK_());
        Var var11 = const__25;
        var11.setMeta((IPersistentMap)const__27);
        Var var12 = var11;
        var11.bindRoot((Object)new anomalies$incorrect_QMARK_());
        Var var13 = const__28;
        var13.setMeta((IPersistentMap)const__30);
        Var var14 = var13;
        var13.bindRoot((Object)new anomalies$conflict_QMARK_());
        Var var15 = const__31;
        var15.setMeta((IPersistentMap)const__33);
        Var var16 = var15;
        var15.bindRoot((Object)new anomalies$not_found_QMARK_());
        Var var17 = const__34;
        var17.setMeta((IPersistentMap)const__36);
        Var var18 = var17;
        var17.bindRoot((Object)new anomalies$fault_QMARK_());
        Var var19 = const__37;
        var19.setMeta((IPersistentMap)const__39);
        Var var20 = var19;
        var19.bindRoot((Object)new anomalies$busy_QMARK_());
        Var var21 = const__40;
        var21.setMeta((IPersistentMap)const__42);
        Var var22 = var21;
        var21.bindRoot((Object)new anomalies$unavailable_QMARK_());
        Var var23 = const__43;
        var23.setMeta((IPersistentMap)const__45);
        Var var24 = var23;
        var23.bindRoot((Object)new anomalies$ok__GT_());
        const__43.setMacro();
        Object v28 = null;
        Var var25 = const__43;
        Var var26 = const__46;
        var26.setMeta((IPersistentMap)const__48);
        Var var27 = var26;
        var26.bindRoot((Object)new anomalies$fault());
        Var var28 = const__49;
        var28.setMeta((IPersistentMap)const__51);
        Var var29 = var28;
        var28.bindRoot((Object)new anomalies$athrow());
        Var var30 = const__52;
        var30.setMeta((IPersistentMap)const__54);
        Var var31 = var30;
        var30.bindRoot((Object)new anomalies$returning());
        const__52.setMacro();
        Object v36 = null;
        Var var32 = const__52;
        Var var33 = const__55;
        var33.setMeta((IPersistentMap)const__57);
        Var var34 = var33;
        var33.bindRoot((Object)new anomalies$_slet_STAR_());
        Var var35 = const__58;
        var35.setMeta((IPersistentMap)const__60);
        Var var36 = var35;
        var35.bindRoot((Object)new anomalies$slet());
        const__58.setMacro();
        Object v42 = null;
        Var var37 = const__58;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.anomalies");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.core2.anomalies", (String)"forbidden");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"cognitect.anomalies", (String)"category"), RT.keyword((String)"cognitect.anomalies", (String)"forbidden")});
        const__11 = RT.var((String)"datomic.core2.anomalies", (String)"not-found");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"cognitect.anomalies", (String)"category"), RT.keyword((String)"cognitect.anomalies", (String)"not-found")});
        const__15 = RT.var((String)"datomic.core2.anomalies", (String)"anom");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")), Tuple.create((Object)Symbol.intern(null, (String)"x"), (Object)Symbol.intern(null, (String)"context")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.core2.anomalies", (String)"ok?");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.core2.anomalies", (String)"forbidden?");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.core2.anomalies", (String)"incorrect?");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.core2.anomalies", (String)"conflict?");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.core2.anomalies", (String)"not-found?");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"datomic.core2.anomalies", (String)"fault?");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__37 = RT.var((String)"datomic.core2.anomalies", (String)"busy?");
        const__39 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__40 = RT.var((String)"datomic.core2.anomalies", (String)"unavailable?");
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"x")))), RT.keyword(null, (String)"column"), 1});
        const__43 = RT.var((String)"datomic.core2.anomalies", (String)"ok->");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"expr"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"forms")))), RT.keyword(null, (String)"column"), 1});
        const__46 = RT.var((String)"datomic.core2.anomalies", (String)"fault");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"t")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Throwable")}))))), RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"datomic.core2.anomalies", (String)"athrow");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"anom")))), RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.core2.anomalies", (String)"returning");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.core2.anomalies", (String)"-slet*");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"bindings"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.core2.anomalies", (String)"slet");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"bindings"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        anomalies__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.anomalies__init").getClassLoader());
        try {
            anomalies__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


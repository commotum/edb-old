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
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.math$create_exponential;
import datomic.math$fn__479;
import datomic.math$loading__6434__auto____477;
import datomic.math$mean;
import datomic.math$mean_and_stddev;
import datomic.math$median;
import datomic.math$reservoir_sample;
import datomic.math$round;
import datomic.math$rounded_mb;
import datomic.math$sla;
import datomic.math$uniform;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;

public class math__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__7;
    public static final Object const__8;
    public static final Var const__9;
    public static final AFn const__10;
    public static final Var const__12;
    public static final AFn const__13;
    public static final Var const__15;
    public static final AFn const__16;
    public static final Object const__17;
    public static final Var const__18;
    public static final AFn const__19;
    public static final Var const__21;
    public static final AFn const__22;
    public static final Var const__24;
    public static final AFn const__25;
    public static final Var const__28;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__37;
    public static final Var const__38;
    public static final AFn const__40;
    public static final Var const__41;
    public static final AFn const__43;
    public static final Var const__44;
    public static final AFn const__46;
    public static final Var const__47;
    public static final AFn const__49;
    public static final Var const__50;
    public static final AFn const__54;
    public static final Var const__56;
    public static final AFn const__58;
    public static final Var const__59;
    public static final AFn const__61;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new math$loading__6434__auto____477()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new math$fn__479())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__7);
        Var var2 = var;
        var.bindRoot(const__8);
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__10);
        Var var4 = var3;
        var3.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)1024L, (long)1024L)));
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__13);
        Var var6 = var5;
        var5.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)1024L, (long)0x100000L)));
        Var var7 = const__15;
        var7.setMeta((IPersistentMap)const__16);
        Var var8 = var7;
        var7.bindRoot(const__17);
        Var var9 = const__18;
        var9.setMeta((IPersistentMap)const__19);
        Var var10 = var9;
        var9.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)60L, (long)1000L)));
        Var var11 = const__21;
        var11.setMeta((IPersistentMap)const__22);
        Var var12 = var11;
        var11.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)60L, (long)60000L)));
        Var var13 = const__24;
        var13.setMeta((IPersistentMap)const__25);
        Var var14 = var13;
        var13.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)24L, (long)3600000L)));
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__31);
        Var var16 = var15;
        var15.bindRoot((Object)new math$mean());
        Var var17 = const__32;
        var17.setMeta((IPersistentMap)const__34);
        Var var18 = var17;
        var17.bindRoot((Object)new math$mean_and_stddev());
        Var var19 = const__35;
        var19.setMeta((IPersistentMap)const__37);
        Var var20 = var19;
        var19.bindRoot((Object)new math$sla());
        Var var21 = const__38;
        var21.setMeta((IPersistentMap)const__40);
        Var var22 = var21;
        var21.bindRoot((Object)new math$median());
        Var var23 = const__41;
        var23.setMeta((IPersistentMap)const__43);
        Var var24 = var23;
        var23.bindRoot((Object)new math$round());
        Var var25 = const__44;
        var25.setMeta((IPersistentMap)const__46);
        Var var26 = var25;
        var25.bindRoot((Object)new math$rounded_mb());
        Var var27 = const__47;
        var27.setMeta((IPersistentMap)const__49);
        Var var28 = var27;
        var27.bindRoot((Object)new math$create_exponential());
        Var var29 = const__50.setDynamic(true);
        var29.setMeta((IPersistentMap)const__54);
        Var var30 = var29;
        var29.bindRoot((Object)new Random(42L));
        Var var31 = const__56;
        var31.setMeta((IPersistentMap)const__58);
        Var var32 = var31;
        var31.bindRoot((Object)new math$uniform());
        Var var33 = const__59;
        var33.setMeta((IPersistentMap)const__61);
        Var var34 = var33;
        var33.bindRoot((Object)new math$reservoir_sample());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.math");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.math", (String)"K");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__8 = 1024L;
        const__9 = RT.var((String)"datomic.math", (String)"M");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.math", (String)"G");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.math", (String)"SECOND");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__17 = 1000L;
        const__18 = RT.var((String)"datomic.math", (String)"MINUTE");
        const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.math", (String)"HOUR");
        const__22 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__24 = RT.var((String)"datomic.math", (String)"DAY");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.math", (String)"mean");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"coll")))), RT.keyword(null, (String)"column"), 1});
        const__32 = RT.var((String)"datomic.math", (String)"mean-and-stddev");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"coll")))), RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.math", (String)"sla");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sorted-coll")))), RT.keyword(null, (String)"column"), 1});
        const__38 = RT.var((String)"datomic.math", (String)"median");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"sorted-coll")))), RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.math", (String)"round");
        const__43 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"num")), Tuple.create((Object)((IObj)Symbol.intern(null, (String)"num")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"double")})), (Object)((IObj)Symbol.intern(null, (String)"significant-digits")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")}))))), RT.keyword(null, (String)"column"), 1});
        const__44 = RT.var((String)"datomic.math", (String)"rounded-mb");
        const__46 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"bytes")))), RT.keyword(null, (String)"column"), 1});
        const__47 = RT.var((String)"datomic.math", (String)"create-exponential");
        const__49 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"x1"), (Object)Symbol.intern(null, (String)"x2"), (Object)Symbol.intern(null, (String)"y1"), (Object)Symbol.intern(null, (String)"y2"))})))), RT.keyword(null, (String)"column"), 1});
        const__50 = RT.var((String)"datomic.math", (String)"*rnd*");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"java.util.Random"), RT.keyword(null, (String)"dynamic"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__56 = RT.var((String)"datomic.math", (String)"uniform");
        const__58 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create()).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), ((IObj)Tuple.create((Object)Symbol.intern(null, (String)"lo"), (Object)Symbol.intern(null, (String)"hi"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long"), RT.keyword(null, (String)"pre"), Tuple.create((Object)((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"<"), Symbol.intern(null, (String)"lo"), Symbol.intern(null, (String)"hi")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 25})))})))), RT.keyword(null, (String)"column"), 1});
        const__59 = RT.var((String)"datomic.math", (String)"reservoir-sample");
        const__61 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ct"), (Object)Symbol.intern(null, (String)"coll")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        math__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.math__init").getClassLoader());
        try {
            math__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


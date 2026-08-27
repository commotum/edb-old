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
import datomic.iter$concat;
import datomic.iter$drop_while;
import datomic.iter$filter;
import datomic.iter$fn__11725;
import datomic.iter$fn__11727;
import datomic.iter$fn__11729;
import datomic.iter$fn__11738;
import datomic.iter$fn__11752;
import datomic.iter$fn__11779;
import datomic.iter$fn__11784;
import datomic.iter$iget;
import datomic.iter$inext;
import datomic.iter$iprev;
import datomic.iter$iter_rseq;
import datomic.iter$iter_seq;
import datomic.iter$iterable;
import datomic.iter$iterator;
import datomic.iter$least_index;
import datomic.iter$loading__6434__auto____11723;
import datomic.iter$map;
import datomic.iter$merge_iters;
import datomic.iter$reduce;
import datomic.iter$reversed_iter;
import datomic.iter$take_while;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class iter__init {
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
    public static final Var const__25;
    public static final AFn const__27;
    public static final Var const__28;
    public static final AFn const__30;
    public static final Var const__31;
    public static final AFn const__35;
    public static final Var const__36;
    public static final AFn const__38;
    public static final Var const__39;
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

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new iter$loading__6434__auto____11723()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new iter$fn__11725())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Object object4 = ((IFn)new iter$fn__11727()).invoke();
        Object object5 = ((IFn)new iter$fn__11729()).invoke();
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new iter$iterator());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new iter$iterable());
        Object object6 = ((IFn)new iter$fn__11738()).invoke();
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new iter$map());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new iter$iter_seq());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new iter$iter_rseq());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new iter$reduce());
        Object object7 = ((IFn)new iter$fn__11752()).invoke();
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new iter$concat());
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot((Object)new iter$filter());
        Var var17 = const__31;
        var17.setMeta((IPersistentMap)const__35);
        Var var18 = var17;
        var17.bindRoot((Object)new iter$take_while());
        Var var19 = const__36;
        var19.setMeta((IPersistentMap)const__38);
        Var var20 = var19;
        var19.bindRoot((Object)new iter$drop_while());
        Var var21 = const__39;
        var21.setMeta((IPersistentMap)const__42);
        Var var22 = var21;
        var21.bindRoot((Object)new iter$least_index());
        Object object8 = ((IFn)new iter$fn__11779()).invoke();
        Object object9 = ((IFn)new iter$fn__11784()).invoke();
        Var var23 = const__43;
        var23.setMeta((IPersistentMap)const__45);
        Var var24 = var23;
        var23.bindRoot((Object)new iter$reversed_iter());
        Var var25 = const__46;
        var25.setMeta((IPersistentMap)const__48);
        Var var26 = var25;
        var25.bindRoot((Object)new iter$merge_iters());
        Var var27 = const__49;
        var27.setMeta((IPersistentMap)const__51);
        Var var28 = var27;
        var27.bindRoot((Object)new iter$iget());
        Var var29 = const__52;
        var29.setMeta((IPersistentMap)const__54);
        Var var30 = var29;
        var29.bindRoot((Object)new iter$inext());
        Var var31 = const__55;
        var31.setMeta((IPersistentMap)const__57);
        Var var32 = var31;
        var31.bindRoot((Object)new iter$iprev());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.iter");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.iter", (String)"iterator");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"iter"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.Iterator")})))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.iter", (String)"iterable");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"iter-fn"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.Iterable")})))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.iter", (String)"map");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"iter")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.iter", (String)"iter-seq");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.iter", (String)"iter-rseq");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.iter", (String)"reduce");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"init"), (Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.iter", (String)"concat");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"iters")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.iter", (String)"filter");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"p"), (Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.iter", (String)"take-while");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.iter.Iter"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"p"), (Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))))), RT.keyword(null, (String)"column"), 1});
        const__36 = RT.var((String)"datomic.iter", (String)"drop-while");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.iter.Iter"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"p"), (Object)Symbol.intern(null, (String)"iter")))), RT.keyword(null, (String)"column"), 1});
        const__39 = RT.var((String)"datomic.iter", (String)"least-index");
        const__42 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cmp")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Comparator")})), (Object)((IObj)Symbol.intern(null, (String)"iters")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"objects")})))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})))), RT.keyword(null, (String)"column"), 1});
        const__43 = RT.var((String)"datomic.iter", (String)"reversed-iter");
        const__45 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"iter")))), RT.keyword(null, (String)"column"), 1});
        const__46 = RT.var((String)"datomic.iter", (String)"merge-iters");
        const__48 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.iter.Iter"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cmp")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Comparator")})), (Object)((IObj)Symbol.intern(null, (String)"iter1")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")})), (Object)((IObj)Symbol.intern(null, (String)"iter2")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))), Tuple.create((Object)Symbol.intern(null, (String)"cmp"), (Object)Symbol.intern(null, (String)"iter1"), (Object)Symbol.intern(null, (String)"iter2"), (Object)Symbol.intern(null, (String)"iter3")), Tuple.create((Object)Symbol.intern(null, (String)"cmp"), (Object)Symbol.intern(null, (String)"iter1"), (Object)Symbol.intern(null, (String)"iter2"), (Object)Symbol.intern(null, (String)"iter3"), (Object)Symbol.intern(null, (String)"iter4")), Tuple.create((Object)Symbol.intern(null, (String)"cmp"), (Object)Symbol.intern(null, (String)"iter1"), (Object)Symbol.intern(null, (String)"iter2"), (Object)Symbol.intern(null, (String)"iter3"), (Object)Symbol.intern(null, (String)"iter4"), (Object)Symbol.intern(null, (String)"iter5")))), RT.keyword(null, (String)"column"), 1});
        const__49 = RT.var((String)"datomic.iter", (String)"iget");
        const__51 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))))), RT.keyword(null, (String)"column"), 1});
        const__52 = RT.var((String)"datomic.iter", (String)"inext");
        const__54 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.iter.Iter"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))))), RT.keyword(null, (String)"column"), 1});
        const__55 = RT.var((String)"datomic.iter", (String)"iprev");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"datomic.iter.Iter"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"iter")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iter")}))))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        iter__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.iter__init").getClassLoader());
        try {
            iter__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


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
package datomic;

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
import datomic.qtune$aug;
import datomic.qtune$cbinds;
import datomic.qtune$cvars;
import datomic.qtune$fn__23417;
import datomic.qtune$loading__6434__auto____23415;
import datomic.qtune$mapq__GT_listq;
import datomic.qtune$min_ret;
import datomic.qtune$partial_queries;
import datomic.qtune$partial_query;
import datomic.qtune$qtune;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class qtune__init {
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
    public static final AFn const__17;
    public static final Var const__18;
    public static final AFn const__20;
    public static final Var const__21;
    public static final AFn const__23;
    public static final Var const__24;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new qtune$loading__6434__auto____23415()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new qtune$fn__23417())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)new qtune$mapq__GT_listq());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new qtune$cvars());
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot((Object)new qtune$cbinds());
        Var var7 = const__15;
        var7.setMeta((IPersistentMap)const__17);
        Var var8 = var7;
        var7.bindRoot((Object)new qtune$partial_query());
        Var var9 = const__18;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot((Object)new qtune$partial_queries());
        Var var11 = const__21;
        var11.setMeta((IPersistentMap)const__23);
        Var var12 = var11;
        var11.bindRoot((Object)new qtune$min_ret());
        Var var13 = const__24;
        var13.setMeta((IPersistentMap)const__26);
        Var var14 = var13;
        var13.bindRoot((Object)new qtune$aug());
        Var var15 = const__27;
        var15.setMeta((IPersistentMap)const__29);
        Var var16 = var15;
        var15.bindRoot((Object)new qtune$qtune());
        Object v19 = null;
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.qtune");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.qtune", (String)"mapq->listq");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"find"), (Object)Symbol.intern(null, (String)"with"), (Object)Symbol.intern(null, (String)"in"), (Object)Symbol.intern(null, (String)"where"), (Object)Symbol.intern(null, (String)"timeout"))})))), RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.qtune", (String)"cvars");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c")))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.qtune", (String)"cbinds");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"c")))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.qtune", (String)"partial-query");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"qmap"), (Object)Symbol.intern(null, (String)"preds"), (Object)Symbol.intern(null, (String)"clauses"), (Object)Symbol.intern(null, (String)"clause"), (Object)Symbol.intern(null, (String)"rclauses"), (Object)Symbol.intern(null, (String)"allow-cross")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.qtune", (String)"partial-queries");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"query"), (Object)Symbol.intern(null, (String)"preds"), (Object)Symbol.intern(null, (String)"clauses"), (Object)Symbol.intern(null, (String)"rclauses"), (Object)Symbol.intern(null, (String)"allow-cross")))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.qtune", (String)"min-ret");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"qs"), (Object)Symbol.intern(null, (String)"args"), (Object)Symbol.intern(null, (String)"timeout")))), RT.keyword(null, (String)"column"), 1});
        const__24 = RT.var((String)"datomic.qtune", (String)"aug");
        const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"query"), (Object)Symbol.intern(null, (String)"preds"), (Object)Symbol.intern(null, (String)"clauses"), (Object)Symbol.intern(null, (String)"rclauses"), (Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
        const__27 = RT.var((String)"datomic.qtune", (String)"qtune");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"query"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        qtune__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.qtune__init").getClassLoader());
        try {
            qtune__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


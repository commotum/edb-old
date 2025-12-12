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
package datomic.core2.aws;

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
import datomic.core2.aws.ddb$attribute_value;
import datomic.core2.aws.ddb$conditional_put_request;
import datomic.core2.aws.ddb$de_attribute_value;
import datomic.core2.aws.ddb$de_item_map;
import datomic.core2.aws.ddb$fn__20429;
import datomic.core2.aws.ddb$item_map;
import datomic.core2.aws.ddb$loading__6789__auto____20427;
import datomic.core2.aws.ddb$query_range_request;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ddb__init {
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

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new ddb$loading__6789__auto____20427()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new ddb$fn__20429())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)new ddb$attribute_value());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new ddb$item_map());
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot((Object)new ddb$de_attribute_value());
        Var var7 = const__15;
        var7.setMeta((IPersistentMap)const__17);
        Var var8 = var7;
        var7.bindRoot((Object)new ddb$de_item_map());
        Var var9 = const__18;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot((Object)new ddb$conditional_put_request());
        Var var11 = const__21;
        var11.setMeta((IPersistentMap)const__23);
        Var var12 = var11;
        var11.bindRoot((Object)new ddb$query_range_request());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.aws.ddb");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.core2.aws.ddb", (String)"attribute-value");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"v")))), RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.core2.aws.ddb", (String)"item-map");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.core2.aws.ddb", (String)"de-attribute-value");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.core2.aws.ddb", (String)"de-item-map");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.core2.aws.ddb", (String)"conditional-put-request");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"p"), (Object)Symbol.intern(null, (String)"r"), (Object)Symbol.intern(null, (String)"item"))})))), RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"datomic.core2.aws.ddb", (String)"query-range-request");
        const__23 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"p"), (Object)Symbol.intern(null, (String)"r"), (Object)Symbol.intern(null, (String)"attrs"), (Object)Symbol.intern(null, (String)"forward"), (Object)Symbol.intern(null, (String)"limit"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        ddb__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.aws.ddb__init").getClassLoader());
        try {
            ddb__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


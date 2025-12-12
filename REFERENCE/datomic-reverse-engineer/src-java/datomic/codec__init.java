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
import datomic.codec$base_64_literal;
import datomic.codec$bytes__GT_string;
import datomic.codec$decode_64;
import datomic.codec$encode_64;
import datomic.codec$fn__11468;
import datomic.codec$loading__6434__auto____11466;
import datomic.codec$string__GT_bytes;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class codec__init {
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

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new codec$loading__6434__auto____11466()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new codec$fn__11468())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)new codec$string__GT_bytes());
        Var var3 = const__9;
        var3.setMeta((IPersistentMap)const__11);
        Var var4 = var3;
        var3.bindRoot((Object)new codec$bytes__GT_string());
        Var var5 = const__12;
        var5.setMeta((IPersistentMap)const__14);
        Var var6 = var5;
        var5.bindRoot((Object)new codec$encode_64());
        Var var7 = const__15;
        var7.setMeta((IPersistentMap)const__17);
        Var var8 = var7;
        var7.bindRoot((Object)new codec$decode_64());
        Var var9 = const__18;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot((Object)new codec$base_64_literal());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.codec");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.codec", (String)"string->bytes");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"s")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
        const__9 = RT.var((String)"datomic.codec", (String)"bytes->string");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"b")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"bytes")}))))), RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.codec", (String)"encode-64");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"raw"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"bytes")})))), RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.codec", (String)"decode-64");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"coded")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"bytes")}))))), RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"datomic.codec", (String)"base-64-literal");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"literal")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        codec__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.codec__init").getClassLoader());
        try {
            codec__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


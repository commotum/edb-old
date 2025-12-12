/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AReference
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.LockingTransaction
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AReference;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.LockingTransaction;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.validators$allow_keys;
import datomic.validators$fn__11918;
import datomic.validators$loading__6434__auto____11916;
import datomic.validators$require_keys;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class validators__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Var const__5;
    public static final AFn const__10;
    public static final Var const__11;
    public static final AFn const__13;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__3);
        Object object2 = ((IFn)new validators$loading__6434__auto____11916()).invoke();
        if (((Symbol)const__1).equals((Object)const__4)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new validators$fn__11918())));
            v3 = null;
        }
        Var var = const__5;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)new validators$allow_keys());
        Var var3 = const__11;
        var3.setMeta((IPersistentMap)const__13);
        Var var4 = var3;
        var3.bindRoot((Object)new validators$require_keys());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.validators")).withMeta(RT.map((Object[])new Object[0]));
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "Validation helpers. Used in data fns and internally."});
        const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__5 = RT.var((String)"datomic.validators", (String)"allow-keys");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"ks")))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.validators", (String)"require-keys");
        const__13 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m"), (Object)Symbol.intern(null, (String)"&"), (Object)Symbol.intern(null, (String)"ks")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        validators__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.validators__init").getClassLoader());
        try {
            validators__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


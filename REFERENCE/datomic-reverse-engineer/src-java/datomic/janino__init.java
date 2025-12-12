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
import datomic.janino$fn__11928;
import datomic.janino$java_data_fn;
import datomic.janino$loading__6434__auto____11926;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class janino__init {
    public static final Var const__0;
    public static final Var const__1;
    public static final AFn const__2;
    public static final AFn const__3;
    public static final Var const__4;
    public static final AFn const__9;

    public static void load() {
        Object v3;
        Object object = const__0.set((Object)Boolean.TRUE);
        Object object2 = ((IFn)const__1.getRawRoot()).invoke((Object)const__2);
        Object object3 = ((IFn)new janino$loading__6434__auto____11926()).invoke();
        if (((Symbol)const__2).equals((Object)const__3)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new janino$fn__11928())));
            v3 = null;
        }
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new janino$java_data_fn());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__1 = RT.var((String)"clojure.core", (String)"in-ns");
        const__2 = (AFn)Symbol.intern(null, (String)"datomic.janino");
        const__3 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__4 = RT.var((String)"datomic.janino", (String)"java-data-fn");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"params"), (Object)((IObj)Symbol.intern(null, (String)"body")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        janino__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.janino__init").getClassLoader());
        try {
            janino__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


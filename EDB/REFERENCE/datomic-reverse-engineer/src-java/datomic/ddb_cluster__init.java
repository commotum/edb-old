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
import datomic.ddb_cluster$create_connection;
import datomic.ddb_cluster$fn__20500;
import datomic.ddb_cluster$key_path;
import datomic.ddb_cluster$loading__6434__auto____20363;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class ddb_cluster__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final AFn const__9;
    public static final Var const__10;
    public static final AFn const__12;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new ddb_cluster$loading__6434__auto____20363()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new ddb_cluster$fn__20500())));
            v2 = null;
        }
        Var var = const__3;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new ddb_cluster$key_path());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new ddb_cluster$create_connection());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.ddb-cluster");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.ddb-cluster", (String)"key-path");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"private"), Boolean.TRUE, RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"path-map"), (Object)Symbol.intern(null, (String)"k")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.ddb-cluster", (String)"create-connection");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"system-root"), (Object)Symbol.intern(null, (String)"params"), (Object)Symbol.intern(null, (String)"region"), (Object)Symbol.intern(null, (String)"override-endpoint")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"cluster-conf")})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        ddb_cluster__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.ddb_cluster__init").getClassLoader());
        try {
            ddb_cluster__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


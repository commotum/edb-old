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
import datomic.s3_kv$fn__23324;
import datomic.s3_kv$fn__23327;
import datomic.s3_kv$loading__6434__auto____23322;
import datomic.s3_kv$s3_storage;
import datomic.s3_kv$s3_storage_path;
import datomic.s3_kv$storage_from_conf_;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class s3_kv__init {
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
    public static final AFn const__17;
    public static final Var const__18;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new s3_kv$loading__6434__auto____23322()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new s3_kv$fn__23324())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new s3_kv$s3_storage_path());
        Object object4 = ((IFn)new s3_kv$fn__23327()).invoke();
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new s3_kv$s3_storage());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new s3_kv$storage_from_conf_());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__17);
        Var var8 = var7;
        var7.bindRoot(((IFn)const__18.getRawRoot()).invoke(const__13.getRawRoot()));
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.s3-kv");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.s3-kv", (String)"s3-storage-path");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"base"), (Object)((IObj)Symbol.intern(null, (String)"k")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.s3-kv", (String)"s3-storage");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"&"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"s3"), (Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"base"))})))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.s3-kv", (String)"storage-from-conf-");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"conf")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.s3-kv", (String)"storage-from-conf");
        const__17 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__18 = RT.var((String)"clojure.core", (String)"memoize");
    }

    static {
        s3_kv__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.s3_kv__init").getClassLoader());
        try {
            s3_kv__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


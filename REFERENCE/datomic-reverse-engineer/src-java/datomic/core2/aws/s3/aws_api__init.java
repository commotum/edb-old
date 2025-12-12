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
package datomic.core2.aws.s3;

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
import datomic.core2.aws.s3.aws_api$delete_object_request;
import datomic.core2.aws.s3.aws_api$fn__20448;
import datomic.core2.aws.s3.aws_api$get_bytes;
import datomic.core2.aws.s3.aws_api$get_object_request;
import datomic.core2.aws.s3.aws_api$loading__6789__auto____20446;
import datomic.core2.aws.s3.aws_api$put_object_request;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class aws_api__init {
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

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new aws_api$loading__6789__auto____20446()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new aws_api$fn__20448())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new aws_api$delete_object_request());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new aws_api$get_object_request());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new aws_api$put_object_request());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new aws_api$get_bytes());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.aws.s3.aws-api");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.core2.aws.s3.aws-api", (String)"delete-object-request");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"key"))})))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.core2.aws.s3.aws-api", (String)"get-object-request");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"key"))})))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.core2.aws.s3.aws-api", (String)"put-object-request");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"body"), (Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"content-length"), (Object)Symbol.intern(null, (String)"key"))})))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.core2.aws.s3.aws-api", (String)"get-bytes");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"bucket"), (Object)Symbol.intern(null, (String)"client"), (Object)Symbol.intern(null, (String)"key"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        aws_api__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.aws.s3.aws_api__init").getClassLoader());
        try {
            aws_api__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


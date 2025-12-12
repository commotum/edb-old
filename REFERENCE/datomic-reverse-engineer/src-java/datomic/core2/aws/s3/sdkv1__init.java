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
package datomic.core2.aws.s3;

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
import datomic.core2.aws.s3.sdkv1$client;
import datomic.core2.aws.s3.sdkv1$delete_object;
import datomic.core2.aws.s3.sdkv1$fn__20524;
import datomic.core2.aws.s3.sdkv1$get_bytes;
import datomic.core2.aws.s3.sdkv1$get_object;
import datomic.core2.aws.s3.sdkv1$loading__6789__auto____20522;
import datomic.core2.aws.s3.sdkv1$put_object;
import datomic.core2.aws.s3.sdkv1$s3_service;
import datomic.core2.aws.s3.sdkv1$s3_service_with_default_retry;
import datomic.core2.aws.s3.sdkv1$throwable__GT_anom;
import datomic.core2.aws.s3.sdkv1$throwable_category;
import datomic.core2.aws.s3.sdkv1$wrap_ex_handler;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class sdkv1__init {
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
    public static final AFn const__33;
    public static final Var const__34;
    public static final AFn const__36;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new sdkv1$loading__6789__auto____20522()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new sdkv1$fn__20524())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new sdkv1$client());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new sdkv1$s3_service());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new sdkv1$s3_service_with_default_retry());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new sdkv1$throwable_category());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new sdkv1$throwable__GT_anom());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new sdkv1$wrap_ex_handler());
        Var var13 = const__25;
        var13.setMeta((IPersistentMap)const__27);
        Var var14 = var13;
        var13.bindRoot((Object)new sdkv1$put_object());
        Var var15 = const__28;
        var15.setMeta((IPersistentMap)const__30);
        Var var16 = var15;
        var15.bindRoot((Object)new sdkv1$get_object());
        Var var17 = const__31;
        var17.setMeta((IPersistentMap)const__33);
        Var var18 = var17;
        var17.bindRoot((Object)new sdkv1$get_bytes());
        Var var19 = const__34;
        var19.setMeta((IPersistentMap)const__36);
        Var var20 = var19;
        var19.bindRoot((Object)new sdkv1$delete_object());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.core2.aws.s3.sdkv1");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"client");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"region"), (Object)((IObj)Symbol.intern(null, (String)"retryPolicy")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"RetryPolicy")})), (Object)((IObj)Symbol.intern(null, (String)"client-conf")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientConfiguration")})), (Object)Symbol.intern(null, (String)"creds-provider")), RT.keyword(null, (String)"or"), RT.map((Object[])new Object[]{Symbol.intern(null, (String)"client-conf"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"ClientConfiguration.")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 33})), Symbol.intern(null, (String)"creds-provider"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"DefaultAWSCredentialsProviderChain.")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 27}))})})))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"s3-service");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"s3-service-with-default-retry");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"throwable-category");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"t")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Throwable")}))))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"throwable->anom");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"t")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Throwable")}))))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"wrap-ex-handler");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"f")), Tuple.create((Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"context")))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"put-object");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"s3")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"AmazonS3Client")})), (Object)((IObj)Symbol.intern(null, (String)"bucket")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)((IObj)Symbol.intern(null, (String)"path")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)Symbol.intern(null, (String)"stream"), (Object)Symbol.intern(null, (String)"metadata")))), RT.keyword(null, (String)"column"), 1});
        const__28 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"get-object");
        const__30 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"s3")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"AmazonS3Client")})), (Object)((IObj)Symbol.intern(null, (String)"bucket")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)((IObj)Symbol.intern(null, (String)"path")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
        const__31 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"get-bytes");
        const__33 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"s3")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"AmazonS3Client")})), (Object)((IObj)Symbol.intern(null, (String)"bucket")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)((IObj)Symbol.intern(null, (String)"path")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
        const__34 = RT.var((String)"datomic.core2.aws.s3.sdkv1", (String)"delete-object");
        const__36 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"s3")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"AmazonS3Client")})), (Object)((IObj)Symbol.intern(null, (String)"bucket")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")})), (Object)((IObj)Symbol.intern(null, (String)"path")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"String")}))))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        sdkv1__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.aws.s3.sdkv1__init").getClassLoader());
        try {
            sdkv1__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


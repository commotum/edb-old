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
 *  clojure.lang.MultiFn
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
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.aws$aws_access_key_id_QMARK_;
import datomic.aws$aws_secret_key_QMARK_;
import datomic.aws$client_config;
import datomic.aws$credentials;
import datomic.aws$credentials_QMARK_;
import datomic.aws$defclient;
import datomic.aws$endpoint_for;
import datomic.aws$fn__17376;
import datomic.aws$fn__17391;
import datomic.aws$loading__6434__auto____17374;
import datomic.aws$newclient;
import datomic.aws$set_endpoint;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class aws__init {
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
    public static final AFn const__28;
    public static final Var const__29;
    public static final AFn const__31;
    public static final Var const__32;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__37;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new aws$loading__6434__auto____17374()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new aws$fn__17376())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new aws$aws_access_key_id_QMARK_());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new aws$aws_secret_key_QMARK_());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new aws$credentials_QMARK_());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new aws$set_endpoint());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot((Object)new aws$credentials());
        Var var11 = const__22;
        var11.setMeta((IPersistentMap)const__24);
        Var var12 = var11;
        var11.bindRoot((Object)new aws$endpoint_for());
        MultiFn multiFn = ((MultiFn)const__25.getRawRoot()).addMethod((Object)const__28, (IFn)new aws$fn__17391());
        Var var13 = const__29;
        var13.setMeta((IPersistentMap)const__31);
        Var var14 = var13;
        var13.bindRoot((Object)new aws$newclient());
        const__29.setMacro();
        Object v19 = null;
        Var var15 = const__29;
        Var var16 = const__32;
        var16.setMeta((IPersistentMap)const__34);
        Var var17 = var16;
        var16.bindRoot((Object)new aws$defclient());
        const__32.setMacro();
        Object v23 = null;
        Var var18 = const__32;
        Var var19 = const__35;
        var19.setMeta((IPersistentMap)const__37);
        Var var20 = var19;
        var19.bindRoot((Object)new aws$client_config());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.aws");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.aws", (String)"aws-access-key-id?");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.aws", (String)"aws-secret-key?");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"s")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.aws", (String)"credentials?");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.aws", (String)"set-endpoint");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"client")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"com.amazonaws.AmazonWebServiceClient")})), (Object)Symbol.intern(null, (String)"endpoint")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.aws", (String)"credentials");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"creds")))), RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.aws", (String)"endpoint-for");
        const__24 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(((IObj)Tuple.create((Object)Symbol.intern(null, (String)"service"), (Object)Symbol.intern(null, (String)"region"))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.lang.String")})))), RT.keyword(null, (String)"column"), 1});
        const__25 = RT.var((String)"datomic.datafy", (String)"data-to-object");
        const__28 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"map"), (Object)RT.classForName((String)"com.amazonaws.ClientConfiguration"));
        const__29 = RT.var((String)"datomic.aws", (String)"newclient");
        const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cls"), (Object)Symbol.intern(null, (String)"creds")), Tuple.create((Object)Symbol.intern(null, (String)"cls"), (Object)Symbol.intern(null, (String)"creds"), (Object)Symbol.intern(null, (String)"conf")))), RT.keyword(null, (String)"column"), 1});
        const__32 = RT.var((String)"datomic.aws", (String)"defclient");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"cls"), (Object)Symbol.intern(null, (String)"service")))), RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.aws", (String)"client-config");
        const__37 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"args")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        aws__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.aws__init").getClassLoader());
        try {
            aws__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


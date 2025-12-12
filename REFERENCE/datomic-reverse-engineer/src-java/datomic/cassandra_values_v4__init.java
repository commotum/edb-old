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
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.cassandra_values_v4$chunk_key;
import datomic.cassandra_values_v4$delete_value;
import datomic.cassandra_values_v4$fn__10273;
import datomic.cassandra_values_v4$fn__10276;
import datomic.cassandra_values_v4$get_value;
import datomic.cassandra_values_v4$loading__6434__auto____10078;
import datomic.cassandra_values_v4$put_value;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class cassandra_values_v4__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final AFn const__8;
    public static final Var const__12;
    public static final AFn const__14;
    public static final Var const__15;
    public static final AFn const__18;
    public static final Var const__19;
    public static final AFn const__20;
    public static final AFn const__26;
    public static final Var const__27;
    public static final AFn const__29;
    public static final Var const__30;
    public static final AFn const__32;
    public static final Var const__33;
    public static final AFn const__35;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new cassandra_values_v4$loading__6434__auto____10078()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new cassandra_values_v4$fn__10273())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__8);
        Var var2 = var;
        var.bindRoot((Object)Numbers.num((long)Numbers.multiply((long)350L, (long)1024L)));
        Var var3 = const__12.setDynamic(true);
        Var var4 = var3;
        var3.setMeta((IPersistentMap)const__14);
        Var var5 = const__15;
        var5.setMeta((IPersistentMap)const__18);
        Var var6 = var5;
        var5.bindRoot((Object)new cassandra_values_v4$chunk_key());
        Object object4 = ((IFn)new cassandra_values_v4$fn__10276()).invoke();
        Var var7 = const__19;
        var7.setMeta((IPersistentMap)const__20);
        Var var8 = var7;
        var7.bindRoot((Object)const__26);
        Var var9 = const__27;
        var9.setMeta((IPersistentMap)const__29);
        Var var10 = var9;
        var9.bindRoot((Object)new cassandra_values_v4$put_value());
        Var var11 = const__30;
        var11.setMeta((IPersistentMap)const__32);
        Var var12 = var11;
        var11.bindRoot((Object)new cassandra_values_v4$get_value());
        Var var13 = const__33;
        var13.setMeta((IPersistentMap)const__35);
        Var var14 = var13;
        var13.bindRoot((Object)new cassandra_values_v4$delete_value());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.cassandra-values-v4");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.cassandra-values-v4", (String)"CHUNK_SIZE");
        const__8 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"const"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__12 = RT.var((String)"datomic.cassandra-values-v4", (String)"*retry*");
        const__14 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"dynamic"), Boolean.TRUE, RT.keyword(null, (String)"column"), 1});
        const__15 = RT.var((String)"datomic.cassandra-values-v4", (String)"chunk-key");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"k"), (Object)Symbol.intern(null, (String)"n")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.cassandra-values-v4", (String)"cql-keys");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__26 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"id2"), (Object)RT.keyword(null, (String)"rev"), (Object)RT.keyword(null, (String)"map"), (Object)RT.keyword(null, (String)"val"), (Object)RT.keyword(null, (String)"chunks"));
        const__27 = RT.var((String)"datomic.cassandra-values-v4", (String)"put-value");
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"session"), (Object)Symbol.intern(null, (String)"table"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"rev"), (Object)((IObj)Symbol.intern(null, (String)"v")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ByteBuffer")}))), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"v-map")})))), RT.keyword(null, (String)"column"), 1});
        const__30 = RT.var((String)"datomic.cassandra-values-v4", (String)"get-value");
        const__32 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"session"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
        const__33 = RT.var((String)"datomic.cassandra-values-v4", (String)"delete-value");
        const__35 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"session"), (Object)Symbol.intern(null, (String)"table"), (Object)Symbol.intern(null, (String)"id")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        cassandra_values_v4__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.cassandra_values_v4__init").getClassLoader());
        try {
            cassandra_values_v4__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


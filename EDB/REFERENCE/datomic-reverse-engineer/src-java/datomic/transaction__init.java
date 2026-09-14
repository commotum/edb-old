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
import datomic.transaction$add_to_log_event_BANG_;
import datomic.transaction$create_procargs;
import datomic.transaction$fn__15888;
import datomic.transaction$loading__6434__auto____15886;
import datomic.transaction$log_completion_BANG_;
import datomic.transaction$peer_message_type;
import datomic.transaction$push_address;
import datomic.transaction$read_message;
import datomic.transaction$reader;
import datomic.transaction$reify__15899;
import datomic.transaction$reify__15901;
import datomic.transaction$submit_address;
import datomic.transaction$write_handlers;
import datomic.transaction$writer;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class transaction__init {
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
    public static final AFn const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final AFn const__27;
    public static final AFn const__29;
    public static final Var const__30;
    public static final AFn const__34;
    public static final Var const__35;
    public static final AFn const__38;
    public static final Var const__39;
    public static final AFn const__40;
    public static final Var const__41;
    public static final Object const__42;
    public static final Var const__43;
    public static final AFn const__44;
    public static final AFn const__54;
    public static final Var const__55;
    public static final AFn const__57;
    public static final Var const__58;
    public static final AFn const__60;
    public static final Var const__61;
    public static final AFn const__63;
    public static final Var const__64;
    public static final AFn const__66;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new transaction$loading__6434__auto____15886()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new transaction$fn__15888())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        Var var = const__4;
        var.setMeta((IPersistentMap)const__9);
        Var var2 = var;
        var.bindRoot((Object)new transaction$peer_message_type());
        Var var3 = const__10;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot((Object)new transaction$submit_address());
        Var var5 = const__13;
        var5.setMeta((IPersistentMap)const__15);
        Var var6 = var5;
        var5.bindRoot((Object)new transaction$push_address());
        Var var7 = const__16;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot((Object)new transaction$write_handlers());
        Var var9 = const__19;
        var9.setMeta((IPersistentMap)const__20);
        Var var10 = var9;
        var9.bindRoot(((IFn)const__21.getRawRoot()).invoke(const__22.getRawRoot(), const__23.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])new Object[]{"dbid", ((IObj)new transaction$reify__15899(null)).withMeta((IPersistentMap)const__27), "datum", ((IObj)new transaction$reify__15901(null)).withMeta((IPersistentMap)const__29)})));
        Var var11 = const__30;
        var11.setMeta((IPersistentMap)const__34);
        Var var12 = var11;
        var11.bindRoot((Object)new transaction$writer());
        Var var13 = const__35;
        var13.setMeta((IPersistentMap)const__38);
        Var var14 = var13;
        var13.bindRoot((Object)new transaction$reader());
        Var var15 = const__39;
        var15.setMeta((IPersistentMap)const__40);
        Var var16 = var15;
        var15.bindRoot(((IFn)const__41.getRawRoot()).invoke(const__42));
        Var var17 = const__43;
        var17.setMeta((IPersistentMap)const__44);
        Var var18 = var17;
        var17.bindRoot((Object)const__54);
        Var var19 = const__55;
        var19.setMeta((IPersistentMap)const__57);
        Var var20 = var19;
        var19.bindRoot((Object)new transaction$log_completion_BANG_());
        Var var21 = const__58;
        var21.setMeta((IPersistentMap)const__60);
        Var var22 = var21;
        var21.bindRoot((Object)new transaction$add_to_log_event_BANG_());
        Var var23 = const__61;
        var23.setMeta((IPersistentMap)const__63);
        Var var24 = var23;
        var23.bindRoot((Object)new transaction$read_message());
        Var var25 = const__64;
        var25.setMeta((IPersistentMap)const__66);
        Var var26 = var25;
        var25.bindRoot((Object)new transaction$create_procargs());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.transaction");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.transaction", (String)"peer-message-type");
        const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"msg")))), RT.keyword(null, (String)"column"), 1});
        const__10 = RT.var((String)"datomic.transaction", (String)"submit-address");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db-name")))), RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.transaction", (String)"push-address");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"db-name")))), RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"datomic.transaction", (String)"write-handlers");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create(), Tuple.create((Object)Symbol.intern(null, (String)"cache")))), RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.transaction", (String)"read-handlers");
        const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__21 = RT.var((String)"clojure.core", (String)"merge");
        const__22 = RT.var((String)"datomic.fressian", (String)"user-read-handlers");
        const__23 = RT.var((String)"datomic.index", (String)"index-read-handlers");
        const__27 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 74, RT.keyword(null, (String)"column"), 5});
        const__29 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 77, RT.keyword(null, (String)"column"), 5});
        const__30 = RT.var((String)"datomic.transaction", (String)"writer");
        const__34 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.fressian.Writer"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"out")), Tuple.create((Object)Symbol.intern(null, (String)"out"), (Object)Symbol.intern(null, (String)"cache")))), RT.keyword(null, (String)"column"), 1});
        const__35 = RT.var((String)"datomic.transaction", (String)"reader");
        const__38 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), RT.classForName((String)"org.fressian.Reader"), RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)((IObj)Symbol.intern(null, (String)"in")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"InputStream")}))))), RT.keyword(null, (String)"column"), 1});
        const__39 = RT.var((String)"datomic.transaction", (String)"log-event-map");
        const__40 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__41 = RT.var((String)"datomic.cache", (String)"create-limited");
        const__42 = 100L;
        const__43 = RT.var((String)"datomic.transaction", (String)"loggable-keys");
        const__44 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__54 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword(null, (String)"txid"), RT.keyword(null, (String)"t"), RT.keyword(null, (String)"msec"), RT.keyword(null, (String)"apply-msec"), RT.keyword(null, (String)"datom-count"), RT.keyword(null, (String)"io-stats"), RT.keyword(null, (String)"tx-stats"), RT.keyword(null, (String)"pf-stats")});
        const__55 = RT.var((String)"datomic.transaction", (String)"log-completion!");
        const__57 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"ids")))), RT.keyword(null, (String)"column"), 1});
        const__58 = RT.var((String)"datomic.transaction", (String)"add-to-log-event!");
        const__60 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"m")))), RT.keyword(null, (String)"column"), 1});
        const__61 = RT.var((String)"datomic.transaction", (String)"read-message");
        const__63 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"is")))), RT.keyword(null, (String)"column"), 1});
        const__64 = RT.var((String)"datomic.transaction", (String)"create-procargs");
        const__66 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"tx"), (Object)Symbol.intern(null, (String)"options")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        transaction__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.transaction__init").getClassLoader());
        try {
            transaction__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


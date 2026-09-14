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
package datomic.core2;

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
import datomic.core2.log$append;
import datomic.core2.log$ensure_tombstone;
import datomic.core2.log$fn__20558;
import datomic.core2.log$loading__6789__auto____20556;
import datomic.core2.log$scan;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class log__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__3;
    public static final AFn const__4;
    public static final Var const__5;
    public static final AFn const__10;
    public static final Var const__11;
    public static final AFn const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final AFn const__16;
    public static final Var const__17;
    public static final AFn const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final AFn const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final AFn const__25;

    public static void load() {
        Object v3;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        IPersistentMap iPersistentMap = ((AReference)Namespace.find((Symbol)((Symbol)const__1))).resetMeta((IPersistentMap)const__3);
        Object object2 = ((IFn)new log$loading__6789__auto____20556()).invoke();
        if (((Symbol)const__1).equals((Object)const__4)) {
            v3 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new log$fn__20558())));
            v3 = null;
        }
        Var var = const__5;
        var.setMeta((IPersistentMap)const__10);
        Var var2 = var;
        var.bindRoot((Object)new log$append());
        Var var3 = const__11;
        var3.setMeta((IPersistentMap)const__12);
        Var var4 = var3;
        var3.bindRoot(const__13.getRawRoot());
        Var var5 = const__14;
        var5.setMeta((IPersistentMap)const__16);
        Var var6 = var5;
        var5.bindRoot((Object)new log$scan());
        Var var7 = const__17;
        var7.setMeta((IPersistentMap)const__18);
        Var var8 = var7;
        var7.bindRoot(const__19.getRawRoot());
        Var var9 = const__20;
        var9.setMeta((IPersistentMap)const__21);
        Var var10 = var9;
        var9.bindRoot(const__22.getRawRoot());
        Var var11 = const__23;
        var11.setMeta((IPersistentMap)const__25);
        Var var12 = var11;
        var11.bindRoot((Object)new log$ensure_tombstone());
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)((IObj)Symbol.intern(null, (String)"datomic.core2.log")).withMeta(RT.map((Object[])new Object[0]));
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"doc"), "API for an append-only log. The log can be used for e.g. Datomic \ntransactions or for a \"durable atom\" that maintains history.\n\nItems in the log have a required header and an optional body. The\nheader is a Clojure map with keys\n\nt          required long, must be montonically ascending\nnext-t     optional long, value for the next t appended\ntombstone  optional string, explains why log can no longer be written\n\nExactly one of next-t/tombstone must be present.\n\nCallers and implementers are free to extend the header map with\nnamespaced keys.\n\nThe body is an optional ByteBuffer.\n\nCallers and implementers share responsibility for correct append\nsemantics. Implementers must fail writes to an existing t with a \nconflict anomaly. Callers must use next-t as the t for a successor\nappend, and can never append after an item with a tombstone.\n"});
        const__4 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__5 = RT.var((String)"datomic.core2.log", (String)"append");
        const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"log"), (Object)Symbol.intern(null, (String)"header")), Tuple.create((Object)Symbol.intern(null, (String)"log"), (Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"t"), (Object)Symbol.intern(null, (String)"next-t")), RT.keyword(null, (String)"as"), Symbol.intern(null, (String)"header")}), (Object)Symbol.intern(null, (String)"body")))), RT.keyword(null, (String)"column"), 1});
        const__11 = RT.var((String)"datomic.core2.log", (String)"delete");
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__13 = RT.var((String)"datomic.core2.log.spi", (String)"-delete");
        const__14 = RT.var((String)"datomic.core2.log", (String)"scan");
        const__16 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"log"), (Object)Symbol.intern(null, (String)"opts")))), RT.keyword(null, (String)"column"), 1});
        const__17 = RT.var((String)"datomic.core2.log", (String)"item-header");
        const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__19 = RT.var((String)"datomic.core2.log.spi", (String)"-item-header");
        const__20 = RT.var((String)"datomic.core2.log", (String)"item-body");
        const__21 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__22 = RT.var((String)"datomic.core2.log.spi", (String)"-item-body");
        const__23 = RT.var((String)"datomic.core2.log", (String)"ensure-tombstone");
        const__25 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"log"), (Object)Symbol.intern(null, (String)"tombstone")))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        log__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.core2.log__init").getClassLoader());
        try {
            log__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


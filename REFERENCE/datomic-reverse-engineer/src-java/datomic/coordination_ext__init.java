/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.Compiler
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.LockingTransaction
 *  clojure.lang.MultiFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.coordination_ext$fn__16739;
import datomic.coordination_ext$fn__16741;
import datomic.coordination_ext$fn__16743;
import datomic.coordination_ext$fn__16745;
import datomic.coordination_ext$fn__16747;
import datomic.coordination_ext$fn__16750;
import datomic.coordination_ext$fn__16752;
import datomic.coordination_ext$fn__16754;
import datomic.coordination_ext$fn__16756;
import datomic.coordination_ext$fn__16758;
import datomic.coordination_ext$fn__16763;
import datomic.coordination_ext$fn__16765;
import datomic.coordination_ext$loading__6434__auto____16671;
import java.util.concurrent.Callable;

public class coordination_ext__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Keyword const__11;
    public static final Var const__12;
    public static final AFn const__15;
    public static final Var const__16;
    public static final Keyword const__17;
    public static final Var const__18;
    public static final Keyword const__19;
    public static final Var const__20;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new coordination_ext$loading__6434__auto____16671()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new coordination_ext$fn__16739())));
            v2 = null;
        }
        MultiFn multiFn = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__4, (IFn)new coordination_ext$fn__16741());
        MultiFn multiFn2 = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__5, (IFn)new coordination_ext$fn__16743());
        MultiFn multiFn3 = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__6, (IFn)new coordination_ext$fn__16745());
        MultiFn multiFn4 = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__7, (IFn)new coordination_ext$fn__16747());
        MultiFn multiFn5 = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__8, (IFn)new coordination_ext$fn__16750());
        MultiFn multiFn6 = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__9, (IFn)new coordination_ext$fn__16752());
        MultiFn multiFn7 = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__10, (IFn)new coordination_ext$fn__16754());
        MultiFn multiFn8 = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__11, (IFn)new coordination_ext$fn__16756());
        Var var = const__12;
        var.setMeta((IPersistentMap)const__15);
        Var var2 = var;
        var.bindRoot(((IFn)const__16.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY));
        MultiFn multiFn9 = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__17, (IFn)new coordination_ext$fn__16758());
        MultiFn multiFn10 = ((MultiFn)const__18.getRawRoot()).addMethod((Object)const__19, (IFn)new coordination_ext$fn__16763());
        MultiFn multiFn11 = ((MultiFn)const__3.getRawRoot()).addMethod((Object)const__19, (IFn)new coordination_ext$fn__16765());
        Object object3 = ((IFn)const__20.getRawRoot()).invoke();
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.coordination-ext");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"datomic.coordination", (String)"create-cluster");
        const__4 = RT.keyword(null, (String)"ddb");
        const__5 = RT.keyword(null, (String)"ddb+s3");
        const__6 = RT.keyword(null, (String)"ddb-local");
        const__7 = RT.keyword(null, (String)"inf");
        const__8 = RT.keyword(null, (String)"cass");
        const__9 = RT.keyword(null, (String)"cass2");
        const__10 = RT.keyword(null, (String)"cass3");
        const__11 = RT.keyword(null, (String)"couchbase");
        const__12 = RT.var((String)"datomic.coordination-ext", (String)"remote-sql-stores");
        const__15 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
        const__16 = RT.var((String)"clojure.core", (String)"atom");
        const__17 = RT.keyword(null, (String)"sql");
        const__18 = RT.var((String)"datomic.coordination", (String)"init-protocol");
        const__19 = RT.keyword(null, (String)"dev");
        const__20 = RT.var((String)"datomic.config", (String)"set-pro");
    }

    static {
        coordination_ext__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.coordination_ext__init").getClassLoader());
        try {
            coordination_ext__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


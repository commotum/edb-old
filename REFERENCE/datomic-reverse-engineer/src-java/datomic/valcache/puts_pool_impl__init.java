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
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.valcache;

import clojure.lang.AFn;
import clojure.lang.Compiler;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.LockingTransaction;
import clojure.lang.MultiFn;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.valcache.puts_pool_impl$create_valcache_puts_pool;
import datomic.valcache.puts_pool_impl$fn__9875;
import datomic.valcache.puts_pool_impl$fn__9878;
import datomic.valcache.puts_pool_impl$fn__9881;
import datomic.valcache.puts_pool_impl$fn__9898;
import datomic.valcache.puts_pool_impl$loading__6434__auto____9873;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class puts_pool_impl__init {
    public static final Var const__0;
    public static final AFn const__1;
    public static final AFn const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final AFn const__11;

    public static void load() {
        Object v2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)const__1);
        Object object2 = ((IFn)new puts_pool_impl$loading__6434__auto____9873()).invoke();
        if (((Symbol)const__1).equals((Object)const__2)) {
            v2 = null;
        } else {
            LockingTransaction.runInTransaction((Callable)((Callable)((Object)new puts_pool_impl$fn__9875())));
            v2 = null;
        }
        Object object3 = const__3.set((Object)Boolean.TRUE);
        MultiFn multiFn = ((MultiFn)const__4.getRawRoot()).addMethod((Object)const__5, (IFn)new puts_pool_impl$fn__9878());
        Object object4 = ((IFn)new puts_pool_impl$fn__9881()).invoke();
        Var var = const__6;
        var.setMeta((IPersistentMap)const__11);
        Var var2 = var;
        var.bindRoot((Object)new puts_pool_impl$create_valcache_puts_pool());
        Object object5 = ((IFn)new puts_pool_impl$fn__9898()).invoke();
    }

    public static void __init0() {
        const__0 = RT.var((String)"clojure.core", (String)"in-ns");
        const__1 = (AFn)Symbol.intern(null, (String)"datomic.valcache.puts-pool-impl");
        const__2 = (AFn)Symbol.intern(null, (String)"clojure.core");
        const__3 = RT.var((String)"clojure.core", (String)"*warn-on-reflection*");
        const__4 = RT.var((String)"datomic.valcache.puts-pool", (String)"get-from-put");
        const__5 = RT.keyword(null, (String)"bbuf");
        const__6 = RT.var((String)"datomic.valcache.puts-pool-impl", (String)"create-valcache-puts-pool");
        const__11 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create(), Tuple.create((Object)RT.map((Object[])new Object[]{RT.keyword(null, (String)"keys"), Tuple.create((Object)Symbol.intern(null, (String)"limit"), (Object)Symbol.intern(null, (String)"threads"))})))), RT.keyword(null, (String)"column"), 1});
    }

    static {
        puts_pool_impl__init.__init0();
        Compiler.pushNSandLoader((ClassLoader)RT.classForName((String)"datomic.valcache.puts_pool_impl__init").getClassLoader());
        try {
            puts_pool_impl__init.load();
        }
        catch (Throwable throwable) {
            Var.popThreadBindings();
            throw throwable;
        }
        Var.popThreadBindings();
    }
}


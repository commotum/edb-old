/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Namespace
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.valcache;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IPersistentMap;
import clojure.lang.Namespace;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.valcache.puts_pool_impl$fn__9881$__GT_ValcachePutsPoolImpl__9889;
import java.util.Arrays;

public final class puts_pool_impl$fn__9881
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.valcache.puts-pool-impl", (String)"->ValcachePutsPoolImpl");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"arglists"), PersistentList.create(Arrays.asList(Tuple.create((Object)Symbol.intern(null, (String)"limit"), (Object)Symbol.intern(null, (String)"puts"), (Object)Symbol.intern(null, (String)"pool")))), RT.keyword(null, (String)"column"), 1});
    public static final Object const__6 = RT.classForName((String)"datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl");

    public static Object invokeStatic() {
        ((Namespace)RT.CURRENT_NS.deref()).importClass(RT.classForNameNonLoading((String)"datomic.valcache.puts_pool_impl.ValcachePutsPoolImpl"));
        Var var = const__0;
        var.setMeta((IPersistentMap)const__5);
        var.bindRoot((Object)new puts_pool_impl$fn__9881$__GT_ValcachePutsPoolImpl__9889());
        return const__6;
    }

    public Object invoke() {
        return puts_pool_impl$fn__9881.invokeStatic();
    }
}


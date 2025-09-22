/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.valcache;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.valcache.puts_pool_impl$fn__9898$fn__9899;

public final class puts_pool_impl$fn__9898
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.valcache.puts-pool-impl", (String)"valcache-puts-pool");
    public static final AFn const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 1});

    public static Object invokeStatic() {
        Var var;
        Var v__6457__auto__9902;
        Var var2 = const__0;
        var2.setMeta((IPersistentMap)const__3);
        Var var3 = v__6457__auto__9902 = var2;
        v__6457__auto__9902 = null;
        if (var3.hasRoot()) {
            var = null;
        } else {
            Var var4 = const__0;
            var4.setMeta((IPersistentMap)const__4);
            var = var4;
            var4.bindRoot((Object)new Delay((IFn)new puts_pool_impl$fn__9898$fn__9899()));
        }
        return var;
    }

    public Object invoke() {
        return puts_pool_impl$fn__9898.invokeStatic();
    }
}


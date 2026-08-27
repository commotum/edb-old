/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.valcache;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.valcache.puts_pool.PutsPool;

public final class puts_pool$get_from_queued_put
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__3;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object puts_pool, Object k) {
        v0 = puts_pool;
        puts_pool = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == puts_pool$get_from_queued_put.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof PutsPool)) {
            v1 = v1;
            puts_pool$get_from_queued_put.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = k;
            k = null;
            v3 = puts_pool$get_from_queued_put.const__0.getRawRoot().invoke(v1, v2);
        } else {
            v4 = k;
            k = null;
            v3 = G__9871 = ((PutsPool)v1).get_queued_put(v4);
        }
        if (Util.identical((Object)G__9871, null)) {
            v5 = null;
        } else {
            v6 = puts_pool$get_from_queued_put.__thunk__0__;
            v7 = G__9871;
            G__9871 = null;
            v5 = v6.get(v7);
            if (v6 == v5) {
                puts_pool$get_from_queued_put.__thunk__0__ = puts_pool$get_from_queued_put.__site__0__.fault(v7);
                v5 = G__9871 = puts_pool$get_from_queued_put.__thunk__0__.get(v7);
            }
        }
        if (Util.identical(G__9871, null)) {
            v8 = null;
        } else {
            v9 = G__9871;
            G__9871 = null;
            v8 = ((IFn)puts_pool$get_from_queued_put.const__3.getRawRoot()).invoke(v9);
        }
        return v8;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return puts_pool$get_from_queued_put.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.valcache.puts-pool", (String)"get-queued-put");
        const__3 = RT.var((String)"datomic.valcache.puts-pool", (String)"get-from-put");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"data"));
        __thunk__0__ = __site__0__;
    }
}


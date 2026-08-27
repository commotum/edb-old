/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.valcache;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.concurrent.ThreadFactory;

public final class puts_pool_impl$create_valcache_puts_pool$reify__9894
implements ThreadFactory,
IObj {
    final IPersistentMap __meta;
    Object idx;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"inc");

    public puts_pool_impl$create_valcache_puts_pool$reify__9894(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.idx = object;
    }

    public puts_pool_impl$create_valcache_puts_pool$reify__9894(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new puts_pool_impl$create_valcache_puts_pool$reify__9894(iPersistentMap, this.idx);
    }

    public Thread newThread(Runnable runnable) {
        Runnable runnable2 = runnable;
        runnable = null;
        Thread G__9895 = new Thread(runnable2, (String)((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"valcache-direct-"), ((IFn)const__2.getRawRoot()).invoke(this.idx, const__3.getRawRoot())));
        G__9895.setDaemon(Boolean.TRUE);
        Object var2_2 = null;
        return G__9895;
    }
}


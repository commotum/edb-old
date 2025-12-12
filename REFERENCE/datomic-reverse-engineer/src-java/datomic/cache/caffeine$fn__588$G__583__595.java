/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.MethodImplCache
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.cache;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class caffeine$fn__588$G__583__595
extends AFunction {
    Object G__584;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cache.caffeine.CacheGet");

    public caffeine$fn__588$G__583__595(Object object) {
        this.G__584 = object;
    }

    public Object invoke(Object gf_____593, Object gf__k__594) {
        Object object;
        caffeine$fn__588$G__583__595 this_;
        IFn f__7644__auto__598;
        MethodImplCache cache__7643__auto__597;
        MethodImplCache methodImplCache = cache__7643__auto__597 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__597 = null;
        IFn iFn = f__7644__auto__598 = methodImplCache.fnFor(Util.classOf((Object)gf_____593));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__598;
            f__7644__auto__598 = null;
            Object object2 = gf_____593;
            gf_____593 = null;
            Object object3 = gf__k__594;
            gf__k__594 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____593, const__1, this_.G__584);
            Object object4 = gf_____593;
            gf_____593 = null;
            Object object5 = gf__k__594;
            gf__k__594 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


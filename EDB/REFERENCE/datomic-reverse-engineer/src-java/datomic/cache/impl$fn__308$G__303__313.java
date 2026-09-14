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

public final class impl$fn__308$G__303__313
extends AFunction {
    Object G__304;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cache.impl.CacheKeys");

    public impl$fn__308$G__303__313(Object object) {
        this.G__304 = object;
    }

    public Object invoke(Object gf_____312) {
        Object object;
        impl$fn__308$G__303__313 this_;
        IFn f__7644__auto__316;
        MethodImplCache cache__7643__auto__315;
        MethodImplCache methodImplCache = cache__7643__auto__315 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__315 = null;
        IFn iFn = f__7644__auto__316 = methodImplCache.fnFor(Util.classOf((Object)gf_____312));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__316;
            f__7644__auto__316 = null;
            Object object2 = gf_____312;
            gf_____312 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____312, const__1, this_.G__304);
            Object object3 = gf_____312;
            gf_____312 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


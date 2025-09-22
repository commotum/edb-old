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

public final class impl$fn__346$G__341__351
extends AFunction {
    Object G__342;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cache.impl.CacheRemove");

    public impl$fn__346$G__341__351(Object object) {
        this.G__342 = object;
    }

    public Object invoke(Object gf__c__350) {
        Object object;
        impl$fn__346$G__341__351 this_;
        IFn f__7644__auto__354;
        MethodImplCache cache__7643__auto__353;
        MethodImplCache methodImplCache = cache__7643__auto__353 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__353 = null;
        IFn iFn = f__7644__auto__354 = methodImplCache.fnFor(Util.classOf((Object)gf__c__350));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__354;
            f__7644__auto__354 = null;
            Object object2 = gf__c__350;
            gf__c__350 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__c__350, const__1, this_.G__342);
            Object object3 = gf__c__350;
            gf__c__350 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


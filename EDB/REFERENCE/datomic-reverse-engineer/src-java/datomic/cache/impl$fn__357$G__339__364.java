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

public final class impl$fn__357$G__339__364
extends AFunction {
    Object G__340;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cache.impl.CacheRemove");

    public impl$fn__357$G__339__364(Object object) {
        this.G__340 = object;
    }

    public Object invoke(Object gf__c__362, Object gf__k__363) {
        Object object;
        impl$fn__357$G__339__364 this_;
        IFn f__7644__auto__367;
        MethodImplCache cache__7643__auto__366;
        MethodImplCache methodImplCache = cache__7643__auto__366 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__366 = null;
        IFn iFn = f__7644__auto__367 = methodImplCache.fnFor(Util.classOf((Object)gf__c__362));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__367;
            f__7644__auto__367 = null;
            Object object2 = gf__c__362;
            gf__c__362 = null;
            Object object3 = gf__k__363;
            gf__k__363 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__c__362, const__1, this_.G__340);
            Object object4 = gf__c__362;
            gf__c__362 = null;
            Object object5 = gf__k__363;
            gf__k__363 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


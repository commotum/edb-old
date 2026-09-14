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

public final class impl$fn__324$G__319__333
extends AFunction {
    Object G__320;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cache.impl.CachePut");

    public impl$fn__324$G__319__333(Object object) {
        this.G__320 = object;
    }

    public Object invoke(Object gf__c__330, Object gf__k__331, Object gf__v__332) {
        Object object;
        impl$fn__324$G__319__333 this_;
        IFn f__7644__auto__336;
        MethodImplCache cache__7643__auto__335;
        MethodImplCache methodImplCache = cache__7643__auto__335 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__335 = null;
        IFn iFn = f__7644__auto__336 = methodImplCache.fnFor(Util.classOf((Object)gf__c__330));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__336;
            f__7644__auto__336 = null;
            Object object2 = gf__c__330;
            gf__c__330 = null;
            Object object3 = gf__k__331;
            gf__k__331 = null;
            Object object4 = gf__v__332;
            gf__v__332 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__c__330, const__1, this_.G__320);
            Object object5 = gf__c__330;
            gf__c__330 = null;
            Object object6 = gf__k__331;
            gf__k__331 = null;
            Object object7 = gf__v__332;
            gf__v__332 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


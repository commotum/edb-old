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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class cluster$fn__10587$G__10450__10594
extends AFunction {
    Object G__10451;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.ClusteredStore");

    public cluster$fn__10587$G__10450__10594(Object object) {
        this.G__10451 = object;
    }

    public Object invoke(Object gf__cs__10592, Object gf__pod_key__10593) {
        Object object;
        cluster$fn__10587$G__10450__10594 this_;
        IFn f__7644__auto__10597;
        MethodImplCache cache__7643__auto__10596;
        MethodImplCache methodImplCache = cache__7643__auto__10596 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10596 = null;
        IFn iFn = f__7644__auto__10597 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10592));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10597;
            f__7644__auto__10597 = null;
            Object object2 = gf__cs__10592;
            gf__cs__10592 = null;
            Object object3 = gf__pod_key__10593;
            gf__pod_key__10593 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10592, const__1, this_.G__10451);
            Object object4 = gf__cs__10592;
            gf__cs__10592 = null;
            Object object5 = gf__pod_key__10593;
            gf__pod_key__10593 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


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

public final class memcached$fn__9982$G__9964__9993
extends AFunction {
    Object G__9965;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.memcached.RecoveringClientImpl");

    public memcached$fn__9982$G__9964__9993(Object object) {
        this.G__9965 = object;
    }

    public Object invoke(Object gf_____9989, Object gf__k__9990, Object gf__ttl__9991, Object gf__v__9992) {
        Object object;
        memcached$fn__9982$G__9964__9993 this_;
        IFn f__7644__auto__9996;
        MethodImplCache cache__7643__auto__9995;
        MethodImplCache methodImplCache = cache__7643__auto__9995 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__9995 = null;
        IFn iFn = f__7644__auto__9996 = methodImplCache.fnFor(Util.classOf((Object)gf_____9989));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__9996;
            f__7644__auto__9996 = null;
            Object object2 = gf_____9989;
            gf_____9989 = null;
            Object object3 = gf__k__9990;
            gf__k__9990 = null;
            Object object4 = gf__ttl__9991;
            gf__ttl__9991 = null;
            Object object5 = gf__v__9992;
            gf__v__9992 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____9989, const__1, this_.G__9965);
            Object object6 = gf_____9989;
            gf_____9989 = null;
            Object object7 = gf__k__9990;
            gf__k__9990 = null;
            Object object8 = gf__ttl__9991;
            gf__ttl__9991 = null;
            Object object9 = gf__v__9992;
            gf__v__9992 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}


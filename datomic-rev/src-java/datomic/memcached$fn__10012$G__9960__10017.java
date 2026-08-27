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

public final class memcached$fn__10012$G__9960__10017
extends AFunction {
    Object G__9961;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.memcached.RecoveringClientImpl");

    public memcached$fn__10012$G__9960__10017(Object object) {
        this.G__9961 = object;
    }

    public Object invoke(Object gf_____10016) {
        Object object;
        memcached$fn__10012$G__9960__10017 this_;
        IFn f__7644__auto__10020;
        MethodImplCache cache__7643__auto__10019;
        MethodImplCache methodImplCache = cache__7643__auto__10019 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10019 = null;
        IFn iFn = f__7644__auto__10020 = methodImplCache.fnFor(Util.classOf((Object)gf_____10016));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10020;
            f__7644__auto__10020 = null;
            Object object2 = gf_____10016;
            gf_____10016 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____10016, const__1, this_.G__9961);
            Object object3 = gf_____10016;
            gf_____10016 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


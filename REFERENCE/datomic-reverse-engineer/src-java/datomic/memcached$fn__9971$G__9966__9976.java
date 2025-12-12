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

public final class memcached$fn__9971$G__9966__9976
extends AFunction {
    Object G__9967;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.memcached.RecoveringClientImpl");

    public memcached$fn__9971$G__9966__9976(Object object) {
        this.G__9967 = object;
    }

    public Object invoke(Object gf_____9975) {
        Object object;
        memcached$fn__9971$G__9966__9976 this_;
        IFn f__7644__auto__9979;
        MethodImplCache cache__7643__auto__9978;
        MethodImplCache methodImplCache = cache__7643__auto__9978 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__9978 = null;
        IFn iFn = f__7644__auto__9979 = methodImplCache.fnFor(Util.classOf((Object)gf_____9975));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__9979;
            f__7644__auto__9979 = null;
            Object object2 = gf_____9975;
            gf_____9975 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____9975, const__1, this_.G__9967);
            Object object3 = gf_____9975;
            gf_____9975 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


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

public final class io$fn__9296$G__9291__9301
extends AFunction {
    Object G__9292;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.io.Coercions");

    public io$fn__9296$G__9291__9301(Object object) {
        this.G__9292 = object;
    }

    public Object invoke(Object gf__x__9300) {
        Object object;
        io$fn__9296$G__9291__9301 this_;
        IFn f__7644__auto__9304;
        MethodImplCache cache__7643__auto__9303;
        MethodImplCache methodImplCache = cache__7643__auto__9303 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__9303 = null;
        IFn iFn = f__7644__auto__9304 = methodImplCache.fnFor(Util.classOf((Object)gf__x__9300));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__9304;
            f__7644__auto__9304 = null;
            Object object2 = gf__x__9300;
            gf__x__9300 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__x__9300, const__1, this_.G__9292);
            Object object3 = gf__x__9300;
            gf__x__9300 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


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

public final class lucene$fn__12275$G__12270__12280
extends AFunction {
    Object G__12271;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.lucene.Readerable");

    public lucene$fn__12275$G__12270__12280(Object object) {
        this.G__12271 = object;
    }

    public Object invoke(Object gf_____12279) {
        Object object;
        lucene$fn__12275$G__12270__12280 this_;
        IFn f__7644__auto__12283;
        MethodImplCache cache__7643__auto__12282;
        MethodImplCache methodImplCache = cache__7643__auto__12282 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12282 = null;
        IFn iFn = f__7644__auto__12283 = methodImplCache.fnFor(Util.classOf((Object)gf_____12279));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12283;
            f__7644__auto__12283 = null;
            Object object2 = gf_____12279;
            gf_____12279 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____12279, const__1, this_.G__12271);
            Object object3 = gf_____12279;
            gf_____12279 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


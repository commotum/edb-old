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

public final class impl$fn__292$G__287__297
extends AFunction {
    Object G__288;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cache.impl.FastCount");

    public impl$fn__292$G__287__297(Object object) {
        this.G__288 = object;
    }

    public Object invoke(Object gf_____296) {
        Object object;
        impl$fn__292$G__287__297 this_;
        IFn f__7644__auto__300;
        MethodImplCache cache__7643__auto__299;
        MethodImplCache methodImplCache = cache__7643__auto__299 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__299 = null;
        IFn iFn = f__7644__auto__300 = methodImplCache.fnFor(Util.classOf((Object)gf_____296));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__300;
            f__7644__auto__300 = null;
            Object object2 = gf_____296;
            gf_____296 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____296, const__1, this_.G__288);
            Object object3 = gf_____296;
            gf_____296 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


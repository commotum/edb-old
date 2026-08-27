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
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class fs$fn__21304$G__21269__21315
extends AFunction {
    Object G__21270;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.val_store.fs.Impl");

    public fs$fn__21304$G__21269__21315(Object object) {
        this.G__21270 = object;
    }

    public Object invoke(Object gf_____21311, Object gf__k__21312, Object gf__v__21313, Object gf__opts__21314) {
        Object object;
        fs$fn__21304$G__21269__21315 this_;
        IFn f__8035__auto__21318;
        MethodImplCache cache__8034__auto__21317;
        MethodImplCache methodImplCache = cache__8034__auto__21317 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__21317 = null;
        IFn iFn = f__8035__auto__21318 = methodImplCache.fnFor(Util.classOf((Object)gf_____21311));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__21318;
            f__8035__auto__21318 = null;
            Object object2 = gf_____21311;
            gf_____21311 = null;
            Object object3 = gf__k__21312;
            gf__k__21312 = null;
            Object object4 = gf__v__21313;
            gf__v__21313 = null;
            Object object5 = gf__opts__21314;
            gf__opts__21314 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21311, const__1, this_.G__21270);
            Object object6 = gf_____21311;
            gf_____21311 = null;
            Object object7 = gf__k__21312;
            gf__k__21312 = null;
            Object object8 = gf__v__21313;
            gf__v__21313 = null;
            Object object9 = gf__opts__21314;
            gf__opts__21314 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}


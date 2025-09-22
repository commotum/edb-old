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
package datomic.core2.val_store.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class sdkv1$fn__21689$G__21678__21698
extends AFunction {
    Object G__21679;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.val_store.s3.sdkv1.Impl");

    public sdkv1$fn__21689$G__21678__21698(Object object) {
        this.G__21679 = object;
    }

    public Object invoke(Object gf_____21695, Object gf__k__21696, Object gf__opts__21697) {
        Object object;
        sdkv1$fn__21689$G__21678__21698 this_;
        IFn f__8035__auto__21701;
        MethodImplCache cache__8034__auto__21700;
        MethodImplCache methodImplCache = cache__8034__auto__21700 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__21700 = null;
        IFn iFn = f__8035__auto__21701 = methodImplCache.fnFor(Util.classOf((Object)gf_____21695));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__21701;
            f__8035__auto__21701 = null;
            Object object2 = gf_____21695;
            gf_____21695 = null;
            Object object3 = gf__k__21696;
            gf__k__21696 = null;
            Object object4 = gf__opts__21697;
            gf__opts__21697 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21695, const__1, this_.G__21679);
            Object object5 = gf_____21695;
            gf_____21695 = null;
            Object object6 = gf__k__21696;
            gf__k__21696 = null;
            Object object7 = gf__opts__21697;
            gf__opts__21697 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


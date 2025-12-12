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

public final class sdkv1$fn__21704$G__21680__21715
extends AFunction {
    Object G__21681;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.val_store.s3.sdkv1.Impl");

    public sdkv1$fn__21704$G__21680__21715(Object object) {
        this.G__21681 = object;
    }

    public Object invoke(Object gf_____21711, Object gf__k__21712, Object gf__v__21713, Object gf__opts__21714) {
        Object object;
        sdkv1$fn__21704$G__21680__21715 this_;
        IFn f__8035__auto__21718;
        MethodImplCache cache__8034__auto__21717;
        MethodImplCache methodImplCache = cache__8034__auto__21717 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__21717 = null;
        IFn iFn = f__8035__auto__21718 = methodImplCache.fnFor(Util.classOf((Object)gf_____21711));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__21718;
            f__8035__auto__21718 = null;
            Object object2 = gf_____21711;
            gf_____21711 = null;
            Object object3 = gf__k__21712;
            gf__k__21712 = null;
            Object object4 = gf__v__21713;
            gf__v__21713 = null;
            Object object5 = gf__opts__21714;
            gf__opts__21714 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21711, const__1, this_.G__21681);
            Object object6 = gf_____21711;
            gf_____21711 = null;
            Object object7 = gf__k__21712;
            gf__k__21712 = null;
            Object object8 = gf__v__21713;
            gf__v__21713 = null;
            Object object9 = gf__opts__21714;
            gf__opts__21714 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}


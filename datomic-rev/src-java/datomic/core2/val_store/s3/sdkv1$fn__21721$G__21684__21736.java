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

public final class sdkv1$fn__21721$G__21684__21736
extends AFunction {
    Object G__21685;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.val_store.s3.sdkv1.Impl");

    public sdkv1$fn__21721$G__21684__21736(Object object) {
        this.G__21685 = object;
    }

    public Object invoke(Object gf_____21730, Object gf__f__21731, Object gf__k__21732, Object gf__op__21733, Object gf__opts__21734, Object gf__context__21735) {
        Object object;
        sdkv1$fn__21721$G__21684__21736 this_;
        IFn f__8035__auto__21739;
        MethodImplCache cache__8034__auto__21738;
        MethodImplCache methodImplCache = cache__8034__auto__21738 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__21738 = null;
        IFn iFn = f__8035__auto__21739 = methodImplCache.fnFor(Util.classOf((Object)gf_____21730));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__21739;
            f__8035__auto__21739 = null;
            Object object2 = gf_____21730;
            gf_____21730 = null;
            Object object3 = gf__f__21731;
            gf__f__21731 = null;
            Object object4 = gf__k__21732;
            gf__k__21732 = null;
            Object object5 = gf__op__21733;
            gf__op__21733 = null;
            Object object6 = gf__opts__21734;
            gf__opts__21734 = null;
            Object object7 = gf__context__21735;
            gf__context__21735 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5, object6, object7);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21730, const__1, this_.G__21685);
            Object object8 = gf_____21730;
            gf_____21730 = null;
            Object object9 = gf__f__21731;
            gf__f__21731 = null;
            Object object10 = gf__k__21732;
            gf__k__21732 = null;
            Object object11 = gf__op__21733;
            gf__op__21733 = null;
            Object object12 = gf__opts__21734;
            gf__opts__21734 = null;
            Object object13 = gf__context__21735;
            gf__context__21735 = null;
            this_ = null;
            object = iFn3.invoke(object8, object9, object10, object11, object12, object13);
        }
        return object;
    }
}


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

public final class sdkv1$fn__21742$G__21682__21751
extends AFunction {
    Object G__21683;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.val_store.s3.sdkv1.Impl");

    public sdkv1$fn__21742$G__21682__21751(Object object) {
        this.G__21683 = object;
    }

    public Object invoke(Object gf_____21748, Object gf__k__21749, Object gf__opts__21750) {
        Object object;
        sdkv1$fn__21742$G__21682__21751 this_;
        IFn f__8035__auto__21754;
        MethodImplCache cache__8034__auto__21753;
        MethodImplCache methodImplCache = cache__8034__auto__21753 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__21753 = null;
        IFn iFn = f__8035__auto__21754 = methodImplCache.fnFor(Util.classOf((Object)gf_____21748));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__21754;
            f__8035__auto__21754 = null;
            Object object2 = gf_____21748;
            gf_____21748 = null;
            Object object3 = gf__k__21749;
            gf__k__21749 = null;
            Object object4 = gf__opts__21750;
            gf__opts__21750 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21748, const__1, this_.G__21683);
            Object object5 = gf_____21748;
            gf_____21748 = null;
            Object object6 = gf__k__21749;
            gf__k__21749 = null;
            Object object7 = gf__opts__21750;
            gf__opts__21750 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


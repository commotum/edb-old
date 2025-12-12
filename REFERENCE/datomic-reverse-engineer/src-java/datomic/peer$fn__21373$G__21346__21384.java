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

public final class peer$fn__21373$G__21346__21384
extends AFunction {
    Object G__21347;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.peer.RemoteConnection");

    public peer$fn__21373$G__21346__21384(Object object) {
        this.G__21347 = object;
    }

    public Object invoke(Object gf_____21380, Object gf__cluster_conf__21381, Object gf__endpoint__21382, Object gf__mode__21383) {
        Object object;
        peer$fn__21373$G__21346__21384 this_;
        IFn f__7644__auto__21387;
        MethodImplCache cache__7643__auto__21386;
        MethodImplCache methodImplCache = cache__7643__auto__21386 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21386 = null;
        IFn iFn = f__7644__auto__21387 = methodImplCache.fnFor(Util.classOf((Object)gf_____21380));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21387;
            f__7644__auto__21387 = null;
            Object object2 = gf_____21380;
            gf_____21380 = null;
            Object object3 = gf__cluster_conf__21381;
            gf__cluster_conf__21381 = null;
            Object object4 = gf__endpoint__21382;
            gf__endpoint__21382 = null;
            Object object5 = gf__mode__21383;
            gf__mode__21383 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21380, const__1, this_.G__21347);
            Object object6 = gf_____21380;
            gf_____21380 = null;
            Object object7 = gf__cluster_conf__21381;
            gf__cluster_conf__21381 = null;
            Object object8 = gf__endpoint__21382;
            gf__endpoint__21382 = null;
            Object object9 = gf__mode__21383;
            gf__mode__21383 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}


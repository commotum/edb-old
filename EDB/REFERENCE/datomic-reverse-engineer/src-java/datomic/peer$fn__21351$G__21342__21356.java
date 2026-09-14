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

public final class peer$fn__21351$G__21342__21356
extends AFunction {
    Object G__21343;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.peer.RemoteConnection");

    public peer$fn__21351$G__21342__21356(Object object) {
        this.G__21343 = object;
    }

    public Object invoke(Object gf_____21355) {
        Object object;
        peer$fn__21351$G__21342__21356 this_;
        IFn f__7644__auto__21359;
        MethodImplCache cache__7643__auto__21358;
        MethodImplCache methodImplCache = cache__7643__auto__21358 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21358 = null;
        IFn iFn = f__7644__auto__21359 = methodImplCache.fnFor(Util.classOf((Object)gf_____21355));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21359;
            f__7644__auto__21359 = null;
            Object object2 = gf_____21355;
            gf_____21355 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21355, const__1, this_.G__21343);
            Object object3 = gf_____21355;
            gf_____21355 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


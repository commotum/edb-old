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

public final class peer$fn__21362$G__21344__21367
extends AFunction {
    Object G__21345;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.peer.RemoteConnection");

    public peer$fn__21362$G__21344__21367(Object object) {
        this.G__21345 = object;
    }

    public Object invoke(Object gf_____21366) {
        Object object;
        peer$fn__21362$G__21344__21367 this_;
        IFn f__7644__auto__21370;
        MethodImplCache cache__7643__auto__21369;
        MethodImplCache methodImplCache = cache__7643__auto__21369 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21369 = null;
        IFn iFn = f__7644__auto__21370 = methodImplCache.fnFor(Util.classOf((Object)gf_____21366));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21370;
            f__7644__auto__21370 = null;
            Object object2 = gf_____21366;
            gf_____21366 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21366, const__1, this_.G__21345);
            Object object3 = gf_____21366;
            gf_____21366 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


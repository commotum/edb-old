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

public final class peer$fn__21445$G__21393__21452
extends AFunction {
    Object G__21394;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.peer.TWatcher");

    public peer$fn__21445$G__21393__21452(Object object) {
        this.G__21394 = object;
    }

    public Object invoke(Object gf_____21450, Object gf__t__21451) {
        Object object;
        peer$fn__21445$G__21393__21452 this_;
        IFn f__7644__auto__21455;
        MethodImplCache cache__7643__auto__21454;
        MethodImplCache methodImplCache = cache__7643__auto__21454 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21454 = null;
        IFn iFn = f__7644__auto__21455 = methodImplCache.fnFor(Util.classOf((Object)gf_____21450));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21455;
            f__7644__auto__21455 = null;
            Object object2 = gf_____21450;
            gf_____21450 = null;
            Object object3 = gf__t__21451;
            gf__t__21451 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21450, const__1, this_.G__21394);
            Object object4 = gf_____21450;
            gf_____21450 = null;
            Object object5 = gf__t__21451;
            gf__t__21451 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


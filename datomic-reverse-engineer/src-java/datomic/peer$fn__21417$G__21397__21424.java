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

public final class peer$fn__21417$G__21397__21424
extends AFunction {
    Object G__21398;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.peer.TWatcher");

    public peer$fn__21417$G__21397__21424(Object object) {
        this.G__21398 = object;
    }

    public Object invoke(Object gf_____21422, Object gf__t__21423) {
        Object object;
        peer$fn__21417$G__21397__21424 this_;
        IFn f__7644__auto__21427;
        MethodImplCache cache__7643__auto__21426;
        MethodImplCache methodImplCache = cache__7643__auto__21426 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21426 = null;
        IFn iFn = f__7644__auto__21427 = methodImplCache.fnFor(Util.classOf((Object)gf_____21422));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21427;
            f__7644__auto__21427 = null;
            Object object2 = gf_____21422;
            gf_____21422 = null;
            Object object3 = gf__t__21423;
            gf__t__21423 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21422, const__1, this_.G__21398);
            Object object4 = gf_____21422;
            gf_____21422 = null;
            Object object5 = gf__t__21423;
            gf__t__21423 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


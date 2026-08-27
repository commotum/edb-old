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

public final class peer$fn__21404$G__21399__21411
extends AFunction {
    Object G__21400;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.peer.TWatcher");

    public peer$fn__21404$G__21399__21411(Object object) {
        this.G__21400 = object;
    }

    public Object invoke(Object gf_____21409, Object gf__new_db__21410) {
        Object object;
        peer$fn__21404$G__21399__21411 this_;
        IFn f__7644__auto__21414;
        MethodImplCache cache__7643__auto__21413;
        MethodImplCache methodImplCache = cache__7643__auto__21413 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21413 = null;
        IFn iFn = f__7644__auto__21414 = methodImplCache.fnFor(Util.classOf((Object)gf_____21409));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21414;
            f__7644__auto__21414 = null;
            Object object2 = gf_____21409;
            gf_____21409 = null;
            Object object3 = gf__new_db__21410;
            gf__new_db__21410 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21409, const__1, this_.G__21400);
            Object object4 = gf_____21409;
            gf_____21409 = null;
            Object object5 = gf__new_db__21410;
            gf__new_db__21410 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


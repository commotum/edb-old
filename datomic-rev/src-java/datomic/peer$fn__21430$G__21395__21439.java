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

public final class peer$fn__21430$G__21395__21439
extends AFunction {
    Object G__21396;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.peer.TWatcher");

    public peer$fn__21430$G__21395__21439(Object object) {
        this.G__21396 = object;
    }

    public Object invoke(Object gf_____21436, Object gf__btype__21437, Object gf__t__21438) {
        Object object;
        peer$fn__21430$G__21395__21439 this_;
        IFn f__7644__auto__21442;
        MethodImplCache cache__7643__auto__21441;
        MethodImplCache methodImplCache = cache__7643__auto__21441 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21441 = null;
        IFn iFn = f__7644__auto__21442 = methodImplCache.fnFor(Util.classOf((Object)gf_____21436));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21442;
            f__7644__auto__21442 = null;
            Object object2 = gf_____21436;
            gf_____21436 = null;
            Object object3 = gf__btype__21437;
            gf__btype__21437 = null;
            Object object4 = gf__t__21438;
            gf__t__21438 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21436, const__1, this_.G__21396);
            Object object5 = gf_____21436;
            gf_____21436 = null;
            Object object6 = gf__btype__21437;
            gf__btype__21437 = null;
            Object object7 = gf__t__21438;
            gf__t__21438 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


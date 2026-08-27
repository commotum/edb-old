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

public final class reconnector2$fn__17117$G__17112__17122
extends AFunction {
    Object G__17113;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.reconnector2.Reconnectable");

    public reconnector2$fn__17117$G__17112__17122(Object object) {
        this.G__17113 = object;
    }

    public Object invoke(Object gf_____17121) {
        Object object;
        reconnector2$fn__17117$G__17112__17122 this_;
        IFn f__7644__auto__17125;
        MethodImplCache cache__7643__auto__17124;
        MethodImplCache methodImplCache = cache__7643__auto__17124 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__17124 = null;
        IFn iFn = f__7644__auto__17125 = methodImplCache.fnFor(Util.classOf((Object)gf_____17121));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__17125;
            f__7644__auto__17125 = null;
            Object object2 = gf_____17121;
            gf_____17121 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____17121, const__1, this_.G__17113);
            Object object3 = gf_____17121;
            gf_____17121 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


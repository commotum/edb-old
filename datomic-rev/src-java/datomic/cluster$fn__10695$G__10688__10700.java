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

public final class cluster$fn__10695$G__10688__10700
extends AFunction {
    Object G__10689;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.AsyncWriter");

    public cluster$fn__10695$G__10688__10700(Object object) {
        this.G__10689 = object;
    }

    public Object invoke(Object gf_____10699) {
        Object object;
        cluster$fn__10695$G__10688__10700 this_;
        IFn f__7644__auto__10703;
        MethodImplCache cache__7643__auto__10702;
        MethodImplCache methodImplCache = cache__7643__auto__10702 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10702 = null;
        IFn iFn = f__7644__auto__10703 = methodImplCache.fnFor(Util.classOf((Object)gf_____10699));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10703;
            f__7644__auto__10703 = null;
            Object object2 = gf_____10699;
            gf_____10699 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____10699, const__1, this_.G__10689);
            Object object3 = gf_____10699;
            gf_____10699 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


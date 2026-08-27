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

public final class cluster$fn__10706$G__10690__10711
extends AFunction {
    Object G__10691;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.AsyncWriter");

    public cluster$fn__10706$G__10690__10711(Object object) {
        this.G__10691 = object;
    }

    public Object invoke(Object gf_____10710) {
        Object object;
        cluster$fn__10706$G__10690__10711 this_;
        IFn f__7644__auto__10714;
        MethodImplCache cache__7643__auto__10713;
        MethodImplCache methodImplCache = cache__7643__auto__10713 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10713 = null;
        IFn iFn = f__7644__auto__10714 = methodImplCache.fnFor(Util.classOf((Object)gf_____10710));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10714;
            f__7644__auto__10714 = null;
            Object object2 = gf_____10710;
            gf_____10710 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____10710, const__1, this_.G__10691);
            Object object3 = gf_____10710;
            gf_____10710 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


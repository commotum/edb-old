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

public final class external_sort$fn__14361$G__14352__14366
extends AFunction {
    Object G__14353;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.external_sort.IO");

    public external_sort$fn__14361$G__14352__14366(Object object) {
        this.G__14353 = object;
    }

    public Object invoke(Object gf_____14365) {
        Object object;
        external_sort$fn__14361$G__14352__14366 this_;
        IFn f__7644__auto__14369;
        MethodImplCache cache__7643__auto__14368;
        MethodImplCache methodImplCache = cache__7643__auto__14368 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14368 = null;
        IFn iFn = f__7644__auto__14369 = methodImplCache.fnFor(Util.classOf((Object)gf_____14365));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14369;
            f__7644__auto__14369 = null;
            Object object2 = gf_____14365;
            gf_____14365 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14365, const__1, this_.G__14353);
            Object object3 = gf_____14365;
            gf_____14365 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


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

public final class external_sort$fn__14430$G__14410__14437
extends AFunction {
    Object G__14411;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.external_sort.ExternalSort");

    public external_sort$fn__14430$G__14410__14437(Object object) {
        this.G__14411 = object;
    }

    public Object invoke(Object gf_____14435, Object gf__handler__14436) {
        Object object;
        external_sort$fn__14430$G__14410__14437 this_;
        IFn f__7644__auto__14440;
        MethodImplCache cache__7643__auto__14439;
        MethodImplCache methodImplCache = cache__7643__auto__14439 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14439 = null;
        IFn iFn = f__7644__auto__14440 = methodImplCache.fnFor(Util.classOf((Object)gf_____14435));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14440;
            f__7644__auto__14440 = null;
            Object object2 = gf_____14435;
            gf_____14435 = null;
            Object object3 = gf__handler__14436;
            gf__handler__14436 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14435, const__1, this_.G__14411);
            Object object4 = gf_____14435;
            gf_____14435 = null;
            Object object5 = gf__handler__14436;
            gf__handler__14436 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


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

public final class external_sort$fn__14443$G__14412__14452
extends AFunction {
    Object G__14413;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.external_sort.ExternalSort");

    public external_sort$fn__14443$G__14412__14452(Object object) {
        this.G__14413 = object;
    }

    public Object invoke(Object gf_____14449, Object gf__files__14450, Object gf__handler__14451) {
        Object object;
        external_sort$fn__14443$G__14412__14452 this_;
        IFn f__7644__auto__14455;
        MethodImplCache cache__7643__auto__14454;
        MethodImplCache methodImplCache = cache__7643__auto__14454 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14454 = null;
        IFn iFn = f__7644__auto__14455 = methodImplCache.fnFor(Util.classOf((Object)gf_____14449));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14455;
            f__7644__auto__14455 = null;
            Object object2 = gf_____14449;
            gf_____14449 = null;
            Object object3 = gf__files__14450;
            gf__files__14450 = null;
            Object object4 = gf__handler__14451;
            gf__handler__14451 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14449, const__1, this_.G__14413);
            Object object5 = gf_____14449;
            gf_____14449 = null;
            Object object6 = gf__files__14450;
            gf__files__14450 = null;
            Object object7 = gf__handler__14451;
            gf__handler__14451 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


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

public final class external_sort$fn__14372$G__14354__14379
extends AFunction {
    Object G__14355;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.external_sort.IO");

    public external_sort$fn__14372$G__14354__14379(Object object) {
        this.G__14355 = object;
    }

    public Object invoke(Object gf_____14377, Object gf__f__14378) {
        Object object;
        external_sort$fn__14372$G__14354__14379 this_;
        IFn f__7644__auto__14382;
        MethodImplCache cache__7643__auto__14381;
        MethodImplCache methodImplCache = cache__7643__auto__14381 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14381 = null;
        IFn iFn = f__7644__auto__14382 = methodImplCache.fnFor(Util.classOf((Object)gf_____14377));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14382;
            f__7644__auto__14382 = null;
            Object object2 = gf_____14377;
            gf_____14377 = null;
            Object object3 = gf__f__14378;
            gf__f__14378 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14377, const__1, this_.G__14355);
            Object object4 = gf_____14377;
            gf_____14377 = null;
            Object object5 = gf__f__14378;
            gf__f__14378 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


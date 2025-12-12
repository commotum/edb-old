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

public final class external_sort$fn__14419$G__14414__14424
extends AFunction {
    Object G__14415;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.external_sort.ExternalSort");

    public external_sort$fn__14419$G__14414__14424(Object object) {
        this.G__14415 = object;
    }

    public Object invoke(Object gf_____14423) {
        Object object;
        external_sort$fn__14419$G__14414__14424 this_;
        IFn f__7644__auto__14427;
        MethodImplCache cache__7643__auto__14426;
        MethodImplCache methodImplCache = cache__7643__auto__14426 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14426 = null;
        IFn iFn = f__7644__auto__14427 = methodImplCache.fnFor(Util.classOf((Object)gf_____14423));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14427;
            f__7644__auto__14427 = null;
            Object object2 = gf_____14423;
            gf_____14423 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14423, const__1, this_.G__14415);
            Object object3 = gf_____14423;
            gf_____14423 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


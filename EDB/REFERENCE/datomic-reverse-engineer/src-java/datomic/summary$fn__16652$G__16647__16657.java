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

public final class summary$fn__16652$G__16647__16657
extends AFunction {
    Object G__16648;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.summary.Summary");

    public summary$fn__16652$G__16647__16657(Object object) {
        this.G__16648 = object;
    }

    public Object invoke(Object gf__x__16656) {
        Object object;
        summary$fn__16652$G__16647__16657 this_;
        IFn f__7644__auto__16660;
        MethodImplCache cache__7643__auto__16659;
        MethodImplCache methodImplCache = cache__7643__auto__16659 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16659 = null;
        IFn iFn = f__7644__auto__16660 = methodImplCache.fnFor(Util.classOf((Object)gf__x__16656));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16660;
            f__7644__auto__16660 = null;
            Object object2 = gf__x__16656;
            gf__x__16656 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__x__16656, const__1, this_.G__16648);
            Object object3 = gf__x__16656;
            gf__x__16656 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


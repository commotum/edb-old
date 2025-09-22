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

public final class backup$fn__19978$G__19941__19987
extends AFunction {
    Object G__19942;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.backup.Storage");

    public backup$fn__19978$G__19941__19987(Object object) {
        this.G__19942 = object;
    }

    public Object invoke(Object gf_____19984, Object gf__k__19985, Object gf__buf__19986) {
        Object object;
        backup$fn__19978$G__19941__19987 this_;
        IFn f__7644__auto__19990;
        MethodImplCache cache__7643__auto__19989;
        MethodImplCache methodImplCache = cache__7643__auto__19989 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__19989 = null;
        IFn iFn = f__7644__auto__19990 = methodImplCache.fnFor(Util.classOf((Object)gf_____19984));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__19990;
            f__7644__auto__19990 = null;
            Object object2 = gf_____19984;
            gf_____19984 = null;
            Object object3 = gf__k__19985;
            gf__k__19985 = null;
            Object object4 = gf__buf__19986;
            gf__buf__19986 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____19984, const__1, this_.G__19942);
            Object object5 = gf_____19984;
            gf_____19984 = null;
            Object object6 = gf__k__19985;
            gf__k__19985 = null;
            Object object7 = gf__buf__19986;
            gf__buf__19986 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


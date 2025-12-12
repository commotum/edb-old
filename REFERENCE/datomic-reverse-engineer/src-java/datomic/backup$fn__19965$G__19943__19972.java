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

public final class backup$fn__19965$G__19943__19972
extends AFunction {
    Object G__19944;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.backup.Storage");

    public backup$fn__19965$G__19943__19972(Object object) {
        this.G__19944 = object;
    }

    public Object invoke(Object gf_____19970, Object gf__prefix__19971) {
        Object object;
        backup$fn__19965$G__19943__19972 this_;
        IFn f__7644__auto__19975;
        MethodImplCache cache__7643__auto__19974;
        MethodImplCache methodImplCache = cache__7643__auto__19974 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__19974 = null;
        IFn iFn = f__7644__auto__19975 = methodImplCache.fnFor(Util.classOf((Object)gf_____19970));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__19975;
            f__7644__auto__19975 = null;
            Object object2 = gf_____19970;
            gf_____19970 = null;
            Object object3 = gf__prefix__19971;
            gf__prefix__19971 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____19970, const__1, this_.G__19944);
            Object object4 = gf_____19970;
            gf_____19970 = null;
            Object object5 = gf__prefix__19971;
            gf__prefix__19971 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


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

public final class db$fn__12448$G__12443__12459
extends AFunction {
    Object G__12444;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.db.LocalizeTempid");

    public db$fn__12448$G__12443__12459(Object object) {
        this.G__12444 = object;
    }

    public Object invoke(Object gf_____12455, Object gf__db__12456, Object gf__procargs__12457, Object gf__local_tempids__12458) {
        Object object;
        db$fn__12448$G__12443__12459 this_;
        IFn f__7644__auto__12462;
        MethodImplCache cache__7643__auto__12461;
        MethodImplCache methodImplCache = cache__7643__auto__12461 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12461 = null;
        IFn iFn = f__7644__auto__12462 = methodImplCache.fnFor(Util.classOf((Object)gf_____12455));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12462;
            f__7644__auto__12462 = null;
            Object object2 = gf_____12455;
            gf_____12455 = null;
            Object object3 = gf__db__12456;
            gf__db__12456 = null;
            Object object4 = gf__procargs__12457;
            gf__procargs__12457 = null;
            Object object5 = gf__local_tempids__12458;
            gf__local_tempids__12458 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____12455, const__1, this_.G__12444);
            Object object6 = gf_____12455;
            gf_____12455 = null;
            Object object7 = gf__db__12456;
            gf__db__12456 = null;
            Object object8 = gf__procargs__12457;
            gf__procargs__12457 = null;
            Object object9 = gf__local_tempids__12458;
            gf__local_tempids__12458 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}


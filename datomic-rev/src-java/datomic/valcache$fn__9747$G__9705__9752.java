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

public final class valcache$fn__9747$G__9705__9752
extends AFunction {
    Object G__9706;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.valcache.IServer");

    public valcache$fn__9747$G__9705__9752(Object object) {
        this.G__9706 = object;
    }

    public Object invoke(Object gf__s__9751) {
        Object object;
        valcache$fn__9747$G__9705__9752 this_;
        IFn f__7644__auto__9755;
        MethodImplCache cache__7643__auto__9754;
        MethodImplCache methodImplCache = cache__7643__auto__9754 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__9754 = null;
        IFn iFn = f__7644__auto__9755 = methodImplCache.fnFor(Util.classOf((Object)gf__s__9751));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__9755;
            f__7644__auto__9755 = null;
            Object object2 = gf__s__9751;
            gf__s__9751 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__s__9751, const__1, this_.G__9706);
            Object object3 = gf__s__9751;
            gf__s__9751 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


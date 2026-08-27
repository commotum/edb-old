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

public final class connector$fn__21055$G__20992__21062
extends AFunction {
    Object G__20993;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.connector.NotificationHandler");

    public connector$fn__21055$G__20992__21062(Object object) {
        this.G__20993 = object;
    }

    public Object invoke(Object gf_____21060, Object gf__msg__21061) {
        Object object;
        connector$fn__21055$G__20992__21062 this_;
        IFn f__7644__auto__21065;
        MethodImplCache cache__7643__auto__21064;
        MethodImplCache methodImplCache = cache__7643__auto__21064 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21064 = null;
        IFn iFn = f__7644__auto__21065 = methodImplCache.fnFor(Util.classOf((Object)gf_____21060));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21065;
            f__7644__auto__21065 = null;
            Object object2 = gf_____21060;
            gf_____21060 = null;
            Object object3 = gf__msg__21061;
            gf__msg__21061 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21060, const__1, this_.G__20993);
            Object object4 = gf_____21060;
            gf_____21060 = null;
            Object object5 = gf__msg__21061;
            gf__msg__21061 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


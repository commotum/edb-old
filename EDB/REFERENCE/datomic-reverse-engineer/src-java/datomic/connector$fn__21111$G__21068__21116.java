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

public final class connector$fn__21111$G__21068__21116
extends AFunction {
    Object G__21069;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.connector.TransactorConnector");

    public connector$fn__21111$G__21068__21116(Object object) {
        this.G__21069 = object;
    }

    public Object invoke(Object gf_____21115) {
        Object object;
        connector$fn__21111$G__21068__21116 this_;
        IFn f__7644__auto__21119;
        MethodImplCache cache__7643__auto__21118;
        MethodImplCache methodImplCache = cache__7643__auto__21118 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21118 = null;
        IFn iFn = f__7644__auto__21119 = methodImplCache.fnFor(Util.classOf((Object)gf_____21115));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21119;
            f__7644__auto__21119 = null;
            Object object2 = gf_____21115;
            gf_____21115 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21115, const__1, this_.G__21069);
            Object object3 = gf_____21115;
            gf_____21115 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


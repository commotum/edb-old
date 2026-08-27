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

public final class memcached$fn__9999$G__9962__10006
extends AFunction {
    Object G__9963;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.memcached.RecoveringClientImpl");

    public memcached$fn__9999$G__9962__10006(Object object) {
        this.G__9963 = object;
    }

    public Object invoke(Object gf_____10004, Object gf__k__10005) {
        Object object;
        memcached$fn__9999$G__9962__10006 this_;
        IFn f__7644__auto__10009;
        MethodImplCache cache__7643__auto__10008;
        MethodImplCache methodImplCache = cache__7643__auto__10008 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10008 = null;
        IFn iFn = f__7644__auto__10009 = methodImplCache.fnFor(Util.classOf((Object)gf_____10004));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10009;
            f__7644__auto__10009 = null;
            Object object2 = gf_____10004;
            gf_____10004 = null;
            Object object3 = gf__k__10005;
            gf__k__10005 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____10004, const__1, this_.G__9963);
            Object object4 = gf_____10004;
            gf_____10004 = null;
            Object object5 = gf__k__10005;
            gf__k__10005 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


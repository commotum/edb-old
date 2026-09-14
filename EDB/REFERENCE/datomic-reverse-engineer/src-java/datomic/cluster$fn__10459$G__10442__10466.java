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

public final class cluster$fn__10459$G__10442__10466
extends AFunction {
    Object G__10443;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.ClusteredStore");

    public cluster$fn__10459$G__10442__10466(Object object) {
        this.G__10443 = object;
    }

    public Object invoke(Object gf__cs__10464, Object gf__key__10465) {
        Object object;
        cluster$fn__10459$G__10442__10466 this_;
        IFn f__7644__auto__10469;
        MethodImplCache cache__7643__auto__10468;
        MethodImplCache methodImplCache = cache__7643__auto__10468 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10468 = null;
        IFn iFn = f__7644__auto__10469 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10464));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10469;
            f__7644__auto__10469 = null;
            Object object2 = gf__cs__10464;
            gf__cs__10464 = null;
            Object object3 = gf__key__10465;
            gf__key__10465 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10464, const__1, this_.G__10443);
            Object object4 = gf__cs__10464;
            gf__cs__10464 = null;
            Object object5 = gf__key__10465;
            gf__key__10465 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


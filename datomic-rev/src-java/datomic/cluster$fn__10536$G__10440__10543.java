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

public final class cluster$fn__10536$G__10440__10543
extends AFunction {
    Object G__10441;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.ClusteredStore");

    public cluster$fn__10536$G__10440__10543(Object object) {
        this.G__10441 = object;
    }

    public Object invoke(Object gf__cs__10541, Object gf__val_key__10542) {
        Object object;
        cluster$fn__10536$G__10440__10543 this_;
        IFn f__7644__auto__10546;
        MethodImplCache cache__7643__auto__10545;
        MethodImplCache methodImplCache = cache__7643__auto__10545 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10545 = null;
        IFn iFn = f__7644__auto__10546 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10541));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10546;
            f__7644__auto__10546 = null;
            Object object2 = gf__cs__10541;
            gf__cs__10541 = null;
            Object object3 = gf__val_key__10542;
            gf__val_key__10542 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10541, const__1, this_.G__10441);
            Object object4 = gf__cs__10541;
            gf__cs__10541 = null;
            Object object5 = gf__val_key__10542;
            gf__val_key__10542 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


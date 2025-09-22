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

public final class cluster$fn__10605$G__10600__10614
extends AFunction {
    Object G__10601;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.Get2");

    public cluster$fn__10605$G__10600__10614(Object object) {
        this.G__10601 = object;
    }

    public Object invoke(Object gf__cs__10611, Object gf__val_key__10612, Object gf__opts__10613) {
        Object object;
        cluster$fn__10605$G__10600__10614 this_;
        IFn f__7644__auto__10617;
        MethodImplCache cache__7643__auto__10616;
        MethodImplCache methodImplCache = cache__7643__auto__10616 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10616 = null;
        IFn iFn = f__7644__auto__10617 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10611));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10617;
            f__7644__auto__10617 = null;
            Object object2 = gf__cs__10611;
            gf__cs__10611 = null;
            Object object3 = gf__val_key__10612;
            gf__val_key__10612 = null;
            Object object4 = gf__opts__10613;
            gf__opts__10613 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10611, const__1, this_.G__10601);
            Object object5 = gf__cs__10611;
            gf__cs__10611 = null;
            Object object6 = gf__val_key__10612;
            gf__val_key__10612 = null;
            Object object7 = gf__opts__10613;
            gf__opts__10613 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


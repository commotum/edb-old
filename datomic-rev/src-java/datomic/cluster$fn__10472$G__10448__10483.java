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

public final class cluster$fn__10472$G__10448__10483
extends AFunction {
    Object G__10449;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.ClusteredStore");

    public cluster$fn__10472$G__10448__10483(Object object) {
        this.G__10449 = object;
    }

    public Object invoke(Object gf__cs__10479, Object gf__ref_key__10480, Object gf__rev__10481, Object gf__vkey__10482) {
        Object object;
        cluster$fn__10472$G__10448__10483 this_;
        IFn f__7644__auto__10486;
        MethodImplCache cache__7643__auto__10485;
        MethodImplCache methodImplCache = cache__7643__auto__10485 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10485 = null;
        IFn iFn = f__7644__auto__10486 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10479));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10486;
            f__7644__auto__10486 = null;
            Object object2 = gf__cs__10479;
            gf__cs__10479 = null;
            Object object3 = gf__ref_key__10480;
            gf__ref_key__10480 = null;
            Object object4 = gf__rev__10481;
            gf__rev__10481 = null;
            Object object5 = gf__vkey__10482;
            gf__vkey__10482 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10479, const__1, this_.G__10449);
            Object object6 = gf__cs__10479;
            gf__cs__10479 = null;
            Object object7 = gf__ref_key__10480;
            gf__ref_key__10480 = null;
            Object object8 = gf__rev__10481;
            gf__rev__10481 = null;
            Object object9 = gf__vkey__10482;
            gf__vkey__10482 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}


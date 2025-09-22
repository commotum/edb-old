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

public final class cluster$fn__10523$G__10446__10530
extends AFunction {
    Object G__10447;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.ClusteredStore");

    public cluster$fn__10523$G__10446__10530(Object object) {
        this.G__10447 = object;
    }

    public Object invoke(Object gf__cs__10528, Object gf__ref_key__10529) {
        Object object;
        cluster$fn__10523$G__10446__10530 this_;
        IFn f__7644__auto__10533;
        MethodImplCache cache__7643__auto__10532;
        MethodImplCache methodImplCache = cache__7643__auto__10532 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10532 = null;
        IFn iFn = f__7644__auto__10533 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10528));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10533;
            f__7644__auto__10533 = null;
            Object object2 = gf__cs__10528;
            gf__cs__10528 = null;
            Object object3 = gf__ref_key__10529;
            gf__ref_key__10529 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10528, const__1, this_.G__10447);
            Object object4 = gf__cs__10528;
            gf__cs__10528 = null;
            Object object5 = gf__ref_key__10529;
            gf__ref_key__10529 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


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

public final class aws_monitor$fn__23593$G__23588__23600
extends AFunction {
    Object G__23589;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.aws_monitor.ToMetricData");

    public aws_monitor$fn__23593$G__23588__23600(Object object) {
        this.G__23589 = object;
    }

    public Object invoke(Object gf__v__23598, Object gf__k__23599) {
        Object object;
        aws_monitor$fn__23593$G__23588__23600 this_;
        IFn f__7644__auto__23603;
        MethodImplCache cache__7643__auto__23602;
        MethodImplCache methodImplCache = cache__7643__auto__23602 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__23602 = null;
        IFn iFn = f__7644__auto__23603 = methodImplCache.fnFor(Util.classOf((Object)gf__v__23598));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__23603;
            f__7644__auto__23603 = null;
            Object object2 = gf__v__23598;
            gf__v__23598 = null;
            Object object3 = gf__k__23599;
            gf__k__23599 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__v__23598, const__1, this_.G__23589);
            Object object4 = gf__v__23598;
            gf__v__23598 = null;
            Object object5 = gf__k__23599;
            gf__k__23599 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


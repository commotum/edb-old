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

public final class monitor$fn__503$G__498__508
extends AFunction {
    Object G__499;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.monitor.Metrics");

    public monitor$fn__503$G__498__508(Object object) {
        this.G__499 = object;
    }

    public Object invoke(Object gf_____507) {
        Object object;
        monitor$fn__503$G__498__508 this_;
        IFn f__7644__auto__511;
        MethodImplCache cache__7643__auto__510;
        MethodImplCache methodImplCache = cache__7643__auto__510 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__510 = null;
        IFn iFn = f__7644__auto__511 = methodImplCache.fnFor(Util.classOf((Object)gf_____507));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__511;
            f__7644__auto__511 = null;
            Object object2 = gf_____507;
            gf_____507 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____507, const__1, this_.G__499);
            Object object3 = gf_____507;
            gf_____507 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


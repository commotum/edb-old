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

public final class queue$fn__12076$G__12069__12081
extends AFunction {
    Object G__12070;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.queue.BlockingConsumer");

    public queue$fn__12076$G__12069__12081(Object object) {
        this.G__12070 = object;
    }

    public Object invoke(Object gf__source__12080) {
        Object object;
        queue$fn__12076$G__12069__12081 this_;
        IFn f__7644__auto__12084;
        MethodImplCache cache__7643__auto__12083;
        MethodImplCache methodImplCache = cache__7643__auto__12083 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12083 = null;
        IFn iFn = f__7644__auto__12084 = methodImplCache.fnFor(Util.classOf((Object)gf__source__12080));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12084;
            f__7644__auto__12084 = null;
            Object object2 = gf__source__12080;
            gf__source__12080 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__source__12080, const__1, this_.G__12070);
            Object object3 = gf__source__12080;
            gf__source__12080 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


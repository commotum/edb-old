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

public final class queue$fn__12087$G__12071__12096
extends AFunction {
    Object G__12072;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.queue.BlockingConsumer");

    public queue$fn__12087$G__12071__12096(Object object) {
        this.G__12072 = object;
    }

    public Object invoke(Object gf__source__12093, Object gf__or_else__12094, Object gf__msec__12095) {
        Object object;
        queue$fn__12087$G__12071__12096 this_;
        IFn f__7644__auto__12099;
        MethodImplCache cache__7643__auto__12098;
        MethodImplCache methodImplCache = cache__7643__auto__12098 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12098 = null;
        IFn iFn = f__7644__auto__12099 = methodImplCache.fnFor(Util.classOf((Object)gf__source__12093));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12099;
            f__7644__auto__12099 = null;
            Object object2 = gf__source__12093;
            gf__source__12093 = null;
            Object object3 = gf__or_else__12094;
            gf__or_else__12094 = null;
            Object object4 = gf__msec__12095;
            gf__msec__12095 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__source__12093, const__1, this_.G__12072);
            Object object5 = gf__source__12093;
            gf__source__12093 = null;
            Object object6 = gf__or_else__12094;
            gf__or_else__12094 = null;
            Object object7 = gf__msec__12095;
            gf__msec__12095 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


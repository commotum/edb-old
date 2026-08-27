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

public final class queue$fn__12054$G__12036__12063
extends AFunction {
    Object G__12037;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.queue.BlockingProducer");

    public queue$fn__12054$G__12036__12063(Object object) {
        this.G__12037 = object;
    }

    public Object invoke(Object gf__sink__12060, Object gf__item__12061, Object gf__msec__12062) {
        Object object;
        queue$fn__12054$G__12036__12063 this_;
        IFn f__7644__auto__12066;
        MethodImplCache cache__7643__auto__12065;
        MethodImplCache methodImplCache = cache__7643__auto__12065 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12065 = null;
        IFn iFn = f__7644__auto__12066 = methodImplCache.fnFor(Util.classOf((Object)gf__sink__12060));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12066;
            f__7644__auto__12066 = null;
            Object object2 = gf__sink__12060;
            gf__sink__12060 = null;
            Object object3 = gf__item__12061;
            gf__item__12061 = null;
            Object object4 = gf__msec__12062;
            gf__msec__12062 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__sink__12060, const__1, this_.G__12037);
            Object object5 = gf__sink__12060;
            gf__sink__12060 = null;
            Object object6 = gf__item__12061;
            gf__item__12061 = null;
            Object object7 = gf__msec__12062;
            gf__msec__12062 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


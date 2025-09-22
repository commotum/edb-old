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

public final class queue$fn__12041$G__12034__12048
extends AFunction {
    Object G__12035;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.queue.BlockingProducer");

    public queue$fn__12041$G__12034__12048(Object object) {
        this.G__12035 = object;
    }

    public Object invoke(Object gf__sink__12046, Object gf__item__12047) {
        Object object;
        queue$fn__12041$G__12034__12048 this_;
        IFn f__7644__auto__12051;
        MethodImplCache cache__7643__auto__12050;
        MethodImplCache methodImplCache = cache__7643__auto__12050 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12050 = null;
        IFn iFn = f__7644__auto__12051 = methodImplCache.fnFor(Util.classOf((Object)gf__sink__12046));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12051;
            f__7644__auto__12051 = null;
            Object object2 = gf__sink__12046;
            gf__sink__12046 = null;
            Object object3 = gf__item__12047;
            gf__item__12047 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__sink__12046, const__1, this_.G__12035);
            Object object4 = gf__sink__12046;
            gf__sink__12046 = null;
            Object object5 = gf__item__12047;
            gf__item__12047 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


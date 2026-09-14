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

public final class config$fn__735$G__719__740
extends AFunction {
    Object G__720;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.config.Symbolish");

    public config$fn__735$G__719__740(Object object) {
        this.G__720 = object;
    }

    public Object invoke(Object gf__s__739) {
        Object object;
        config$fn__735$G__719__740 this_;
        IFn f__7644__auto__743;
        MethodImplCache cache__7643__auto__742;
        MethodImplCache methodImplCache = cache__7643__auto__742 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__742 = null;
        IFn iFn = f__7644__auto__743 = methodImplCache.fnFor(Util.classOf((Object)gf__s__739));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__743;
            f__7644__auto__743 = null;
            Object object2 = gf__s__739;
            gf__s__739 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__s__739, const__1, this_.G__720);
            Object object3 = gf__s__739;
            gf__s__739 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


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
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class fs$fn__21289$G__21265__21298
extends AFunction {
    Object G__21266;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.val_store.fs.Impl");

    public fs$fn__21289$G__21265__21298(Object object) {
        this.G__21266 = object;
    }

    public Object invoke(Object gf_____21295, Object gf__k__21296, Object gf__opts__21297) {
        Object object;
        fs$fn__21289$G__21265__21298 this_;
        IFn f__8035__auto__21301;
        MethodImplCache cache__8034__auto__21300;
        MethodImplCache methodImplCache = cache__8034__auto__21300 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__21300 = null;
        IFn iFn = f__8035__auto__21301 = methodImplCache.fnFor(Util.classOf((Object)gf_____21295));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__21301;
            f__8035__auto__21301 = null;
            Object object2 = gf_____21295;
            gf_____21295 = null;
            Object object3 = gf__k__21296;
            gf__k__21296 = null;
            Object object4 = gf__opts__21297;
            gf__opts__21297 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21295, const__1, this_.G__21266);
            Object object5 = gf_____21295;
            gf_____21295 = null;
            Object object6 = gf__k__21296;
            gf__k__21296 = null;
            Object object7 = gf__opts__21297;
            gf__opts__21297 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


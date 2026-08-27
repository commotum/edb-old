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

public final class process$fn__14917$G__14889__14922
extends AFunction {
    Object G__14890;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.process.CriticalFailure");

    public process$fn__14917$G__14889__14922(Object object) {
        this.G__14890 = object;
    }

    public Object invoke(Object gf_____14921) {
        Object object;
        process$fn__14917$G__14889__14922 this_;
        IFn f__7644__auto__14925;
        MethodImplCache cache__7643__auto__14924;
        MethodImplCache methodImplCache = cache__7643__auto__14924 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14924 = null;
        IFn iFn = f__7644__auto__14925 = methodImplCache.fnFor(Util.classOf((Object)gf_____14921));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14925;
            f__7644__auto__14925 = null;
            Object object2 = gf_____14921;
            gf_____14921 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14921, const__1, this_.G__14890);
            Object object3 = gf_____14921;
            gf_____14921 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


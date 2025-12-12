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

public final class process$fn__14928$G__14887__14935
extends AFunction {
    Object G__14888;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.process.CriticalFailure");

    public process$fn__14928$G__14887__14935(Object object) {
        this.G__14888 = object;
    }

    public Object invoke(Object gf_____14933, Object gf__h__14934) {
        Object object;
        process$fn__14928$G__14887__14935 this_;
        IFn f__7644__auto__14938;
        MethodImplCache cache__7643__auto__14937;
        MethodImplCache methodImplCache = cache__7643__auto__14937 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14937 = null;
        IFn iFn = f__7644__auto__14938 = methodImplCache.fnFor(Util.classOf((Object)gf_____14933));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14938;
            f__7644__auto__14938 = null;
            Object object2 = gf_____14933;
            gf_____14933 = null;
            Object object3 = gf__h__14934;
            gf__h__14934 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14933, const__1, this_.G__14888);
            Object object4 = gf_____14933;
            gf_____14933 = null;
            Object object5 = gf__h__14934;
            gf__h__14934 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


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

public final class query$fn__19221$G__19216__19226
extends AFunction {
    Object G__19217;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.query.Immutify");

    public query$fn__19221$G__19216__19226(Object object) {
        this.G__19217 = object;
    }

    public Object invoke(Object gf__x__19225) {
        Object object;
        query$fn__19221$G__19216__19226 this_;
        IFn f__7644__auto__19229;
        MethodImplCache cache__7643__auto__19228;
        MethodImplCache methodImplCache = cache__7643__auto__19228 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__19228 = null;
        IFn iFn = f__7644__auto__19229 = methodImplCache.fnFor(Util.classOf((Object)gf__x__19225));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__19229;
            f__7644__auto__19229 = null;
            Object object2 = gf__x__19225;
            gf__x__19225 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__x__19225, const__1, this_.G__19217);
            Object object3 = gf__x__19225;
            gf__x__19225 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


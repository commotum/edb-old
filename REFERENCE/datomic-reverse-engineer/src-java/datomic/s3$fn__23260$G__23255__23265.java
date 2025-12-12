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

public final class s3$fn__23260$G__23255__23265
extends AFunction {
    Object G__23256;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.s3.Name");

    public s3$fn__23260$G__23255__23265(Object object) {
        this.G__23256 = object;
    }

    public Object invoke(Object gf__x__23264) {
        Object object;
        s3$fn__23260$G__23255__23265 this_;
        IFn f__7644__auto__23268;
        MethodImplCache cache__7643__auto__23267;
        MethodImplCache methodImplCache = cache__7643__auto__23267 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__23267 = null;
        IFn iFn = f__7644__auto__23268 = methodImplCache.fnFor(Util.classOf((Object)gf__x__23264));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__23268;
            f__7644__auto__23268 = null;
            Object object2 = gf__x__23264;
            gf__x__23264 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__x__23264, const__1, this_.G__23256);
            Object object3 = gf__x__23264;
            gf__x__23264 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


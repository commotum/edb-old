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

public final class db$fn__13906$G__13901__13911
extends AFunction {
    Object G__13902;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.db.LocalDb");

    public db$fn__13906$G__13901__13911(Object object) {
        this.G__13902 = object;
    }

    public Object invoke(Object gf__db__13910) {
        Object object;
        db$fn__13906$G__13901__13911 this_;
        IFn f__7644__auto__13914;
        MethodImplCache cache__7643__auto__13913;
        MethodImplCache methodImplCache = cache__7643__auto__13913 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__13913 = null;
        IFn iFn = f__7644__auto__13914 = methodImplCache.fnFor(Util.classOf((Object)gf__db__13910));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__13914;
            f__7644__auto__13914 = null;
            Object object2 = gf__db__13910;
            gf__db__13910 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__db__13910, const__1, this_.G__13902);
            Object object3 = gf__db__13910;
            gf__db__13910 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


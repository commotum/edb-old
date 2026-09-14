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

public final class db$fn__12483$G__12467__12488
extends AFunction {
    Object G__12468;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.db.Symbolish");

    public db$fn__12483$G__12467__12488(Object object) {
        this.G__12468 = object;
    }

    public Object invoke(Object gf__s__12487) {
        Object object;
        db$fn__12483$G__12467__12488 this_;
        IFn f__7644__auto__12491;
        MethodImplCache cache__7643__auto__12490;
        MethodImplCache methodImplCache = cache__7643__auto__12490 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12490 = null;
        IFn iFn = f__7644__auto__12491 = methodImplCache.fnFor(Util.classOf((Object)gf__s__12487));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12491;
            f__7644__auto__12491 = null;
            Object object2 = gf__s__12487;
            gf__s__12487 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__s__12487, const__1, this_.G__12468);
            Object object3 = gf__s__12487;
            gf__s__12487 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


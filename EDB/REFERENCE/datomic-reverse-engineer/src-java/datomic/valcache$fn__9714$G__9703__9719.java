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

public final class valcache$fn__9714$G__9703__9719
extends AFunction {
    Object G__9704;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.valcache.IServer");

    public valcache$fn__9714$G__9703__9719(Object object) {
        this.G__9704 = object;
    }

    public Object invoke(Object gf__s__9718) {
        Object object;
        valcache$fn__9714$G__9703__9719 this_;
        IFn f__7644__auto__9722;
        MethodImplCache cache__7643__auto__9721;
        MethodImplCache methodImplCache = cache__7643__auto__9721 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__9721 = null;
        IFn iFn = f__7644__auto__9722 = methodImplCache.fnFor(Util.classOf((Object)gf__s__9718));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__9722;
            f__7644__auto__9722 = null;
            Object object2 = gf__s__9718;
            gf__s__9718 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__s__9718, const__1, this_.G__9704);
            Object object3 = gf__s__9718;
            gf__s__9718 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


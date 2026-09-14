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

public final class treewalk$fn__19697$G__19677__19702
extends AFunction {
    Object G__19678;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.treewalk.TreeWalker");

    public treewalk$fn__19697$G__19677__19702(Object object) {
        this.G__19678 = object;
    }

    public Object invoke(Object gf_____19701) {
        Object object;
        treewalk$fn__19697$G__19677__19702 this_;
        IFn f__7644__auto__19705;
        MethodImplCache cache__7643__auto__19704;
        MethodImplCache methodImplCache = cache__7643__auto__19704 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__19704 = null;
        IFn iFn = f__7644__auto__19705 = methodImplCache.fnFor(Util.classOf((Object)gf_____19701));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__19705;
            f__7644__auto__19705 = null;
            Object object2 = gf_____19701;
            gf_____19701 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____19701, const__1, this_.G__19678);
            Object object3 = gf_____19701;
            gf_____19701 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


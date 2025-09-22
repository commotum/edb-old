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

public final class treewalk$fn__19666$G__19661__19671
extends AFunction {
    Object G__19662;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.treewalk.NodeId");

    public treewalk$fn__19666$G__19661__19671(Object object) {
        this.G__19662 = object;
    }

    public Object invoke(Object gf_____19670) {
        Object object;
        treewalk$fn__19666$G__19661__19671 this_;
        IFn f__7644__auto__19674;
        MethodImplCache cache__7643__auto__19673;
        MethodImplCache methodImplCache = cache__7643__auto__19673 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__19673 = null;
        IFn iFn = f__7644__auto__19674 = methodImplCache.fnFor(Util.classOf((Object)gf_____19670));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__19674;
            f__7644__auto__19674 = null;
            Object object2 = gf_____19670;
            gf_____19670 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____19670, const__1, this_.G__19662);
            Object object3 = gf_____19670;
            gf_____19670 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


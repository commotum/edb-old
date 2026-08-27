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
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class logged$fn__19689$G__19682__19696
extends AFunction {
    Object G__19683;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.atom.logged.LoggedAtomImpl");

    public logged$fn__19689$G__19682__19696(Object object) {
        this.G__19683 = object;
    }

    public Object invoke(Object gf_____19694, Object gf__v__19695) {
        Object object;
        logged$fn__19689$G__19682__19696 this_;
        IFn f__8035__auto__19699;
        MethodImplCache cache__8034__auto__19698;
        MethodImplCache methodImplCache = cache__8034__auto__19698 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__19698 = null;
        IFn iFn = f__8035__auto__19699 = methodImplCache.fnFor(Util.classOf((Object)gf_____19694));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__19699;
            f__8035__auto__19699 = null;
            Object object2 = gf_____19694;
            gf_____19694 = null;
            Object object3 = gf__v__19695;
            gf__v__19695 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____19694, const__1, this_.G__19683);
            Object object4 = gf_____19694;
            gf_____19694 = null;
            Object object5 = gf__v__19695;
            gf__v__19695 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


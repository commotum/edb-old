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

public final class logged$fn__19702$G__19684__19707
extends AFunction {
    Object G__19685;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.atom.logged.LoggedAtomImpl");

    public logged$fn__19702$G__19684__19707(Object object) {
        this.G__19685 = object;
    }

    public Object invoke(Object gf_____19706) {
        Object object;
        logged$fn__19702$G__19684__19707 this_;
        IFn f__8035__auto__19710;
        MethodImplCache cache__8034__auto__19709;
        MethodImplCache methodImplCache = cache__8034__auto__19709 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__19709 = null;
        IFn iFn = f__8035__auto__19710 = methodImplCache.fnFor(Util.classOf((Object)gf_____19706));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__19710;
            f__8035__auto__19710 = null;
            Object object2 = gf_____19706;
            gf_____19706 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____19706, const__1, this_.G__19685);
            Object object3 = gf_____19706;
            gf_____19706 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


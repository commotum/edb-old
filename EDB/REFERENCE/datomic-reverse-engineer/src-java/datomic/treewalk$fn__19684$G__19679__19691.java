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

public final class treewalk$fn__19684$G__19679__19691
extends AFunction {
    Object G__19680;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.treewalk.TreeWalker");

    public treewalk$fn__19684$G__19679__19691(Object object) {
        this.G__19680 = object;
    }

    public Object invoke(Object gf_____19689, Object gf__ids__GT_nodes__19690) {
        Object object;
        treewalk$fn__19684$G__19679__19691 this_;
        IFn f__7644__auto__19694;
        MethodImplCache cache__7643__auto__19693;
        MethodImplCache methodImplCache = cache__7643__auto__19693 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__19693 = null;
        IFn iFn = f__7644__auto__19694 = methodImplCache.fnFor(Util.classOf((Object)gf_____19689));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__19694;
            f__7644__auto__19694 = null;
            Object object2 = gf_____19689;
            gf_____19689 = null;
            Object object3 = gf__ids__GT_nodes__19690;
            gf__ids__GT_nodes__19690 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____19689, const__1, this_.G__19680);
            Object object4 = gf_____19689;
            gf_____19689 = null;
            Object object5 = gf__ids__GT_nodes__19690;
            gf__ids__GT_nodes__19690 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


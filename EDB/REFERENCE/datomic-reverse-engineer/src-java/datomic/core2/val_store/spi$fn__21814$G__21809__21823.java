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
package datomic.core2.val_store;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class spi$fn__21814$G__21809__21823
extends AFunction {
    Object G__21810;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.val_store.spi.Get");

    public spi$fn__21814$G__21809__21823(Object object) {
        this.G__21810 = object;
    }

    public Object invoke(Object gf_____21820, Object gf__k__21821, Object gf__opts__21822) {
        Object object;
        spi$fn__21814$G__21809__21823 this_;
        IFn f__8035__auto__21826;
        MethodImplCache cache__8034__auto__21825;
        MethodImplCache methodImplCache = cache__8034__auto__21825 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__21825 = null;
        IFn iFn = f__8035__auto__21826 = methodImplCache.fnFor(Util.classOf((Object)gf_____21820));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__21826;
            f__8035__auto__21826 = null;
            Object object2 = gf_____21820;
            gf_____21820 = null;
            Object object3 = gf__k__21821;
            gf__k__21821 = null;
            Object object4 = gf__opts__21822;
            gf__opts__21822 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21820, const__1, this_.G__21810);
            Object object5 = gf_____21820;
            gf_____21820 = null;
            Object object6 = gf__k__21821;
            gf__k__21821 = null;
            Object object7 = gf__opts__21822;
            gf__opts__21822 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


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

public final class spi$fn__21834$G__21829__21843
extends AFunction {
    Object G__21830;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.val_store.spi.Delete");

    public spi$fn__21834$G__21829__21843(Object object) {
        this.G__21830 = object;
    }

    public Object invoke(Object gf_____21840, Object gf__k__21841, Object gf__opts__21842) {
        Object object;
        spi$fn__21834$G__21829__21843 this_;
        IFn f__8035__auto__21846;
        MethodImplCache cache__8034__auto__21845;
        MethodImplCache methodImplCache = cache__8034__auto__21845 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__21845 = null;
        IFn iFn = f__8035__auto__21846 = methodImplCache.fnFor(Util.classOf((Object)gf_____21840));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__21846;
            f__8035__auto__21846 = null;
            Object object2 = gf_____21840;
            gf_____21840 = null;
            Object object3 = gf__k__21841;
            gf__k__21841 = null;
            Object object4 = gf__opts__21842;
            gf__opts__21842 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21840, const__1, this_.G__21830);
            Object object5 = gf_____21840;
            gf_____21840 = null;
            Object object6 = gf__k__21841;
            gf__k__21841 = null;
            Object object7 = gf__opts__21842;
            gf__opts__21842 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


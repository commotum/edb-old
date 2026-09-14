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

public final class spi$fn__21792$G__21787__21803
extends AFunction {
    Object G__21788;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.val_store.spi.Put");

    public spi$fn__21792$G__21787__21803(Object object) {
        this.G__21788 = object;
    }

    public Object invoke(Object gf_____21799, Object gf__k__21800, Object gf__v__21801, Object gf__opts__21802) {
        Object object;
        spi$fn__21792$G__21787__21803 this_;
        IFn f__8035__auto__21806;
        MethodImplCache cache__8034__auto__21805;
        MethodImplCache methodImplCache = cache__8034__auto__21805 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__21805 = null;
        IFn iFn = f__8035__auto__21806 = methodImplCache.fnFor(Util.classOf((Object)gf_____21799));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__21806;
            f__8035__auto__21806 = null;
            Object object2 = gf_____21799;
            gf_____21799 = null;
            Object object3 = gf__k__21800;
            gf__k__21800 = null;
            Object object4 = gf__v__21801;
            gf__v__21801 = null;
            Object object5 = gf__opts__21802;
            gf__opts__21802 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21799, const__1, this_.G__21788);
            Object object6 = gf_____21799;
            gf_____21799 = null;
            Object object7 = gf__k__21800;
            gf__k__21800 = null;
            Object object8 = gf__v__21801;
            gf__v__21801 = null;
            Object object9 = gf__opts__21802;
            gf__opts__21802 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}


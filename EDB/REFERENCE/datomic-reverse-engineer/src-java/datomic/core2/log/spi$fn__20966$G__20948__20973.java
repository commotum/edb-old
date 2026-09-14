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
package datomic.core2.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.MethodImplCache;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class spi$fn__20966$G__20948__20973
extends AFunction {
    Object G__20949;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.log.spi.Item");

    public spi$fn__20966$G__20948__20973(Object object) {
        this.G__20949 = object;
    }

    public Object invoke(Object gf_____20971, Object gf__item__20972) {
        Object object;
        spi$fn__20966$G__20948__20973 this_;
        IFn f__8035__auto__20976;
        MethodImplCache cache__8034__auto__20975;
        MethodImplCache methodImplCache = cache__8034__auto__20975 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__20975 = null;
        IFn iFn = f__8035__auto__20976 = methodImplCache.fnFor(Util.classOf((Object)gf_____20971));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__20976;
            f__8035__auto__20976 = null;
            Object object2 = gf_____20971;
            gf_____20971 = null;
            Object object3 = gf__item__20972;
            gf__item__20972 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20971, const__1, this_.G__20949);
            Object object4 = gf_____20971;
            gf_____20971 = null;
            Object object5 = gf__item__20972;
            gf__item__20972 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


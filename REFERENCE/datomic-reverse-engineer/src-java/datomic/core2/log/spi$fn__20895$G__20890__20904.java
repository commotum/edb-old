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

public final class spi$fn__20895$G__20890__20904
extends AFunction {
    Object G__20891;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.log.spi.Append");

    public spi$fn__20895$G__20890__20904(Object object) {
        this.G__20891 = object;
    }

    public Object invoke(Object gf_____20901, Object gf__header__20902, Object gf__body__20903) {
        Object object;
        spi$fn__20895$G__20890__20904 this_;
        IFn f__8035__auto__20907;
        MethodImplCache cache__8034__auto__20906;
        MethodImplCache methodImplCache = cache__8034__auto__20906 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__20906 = null;
        IFn iFn = f__8035__auto__20907 = methodImplCache.fnFor(Util.classOf((Object)gf_____20901));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__20907;
            f__8035__auto__20907 = null;
            Object object2 = gf_____20901;
            gf_____20901 = null;
            Object object3 = gf__header__20902;
            gf__header__20902 = null;
            Object object4 = gf__body__20903;
            gf__body__20903 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20901, const__1, this_.G__20891);
            Object object5 = gf_____20901;
            gf_____20901 = null;
            Object object6 = gf__header__20902;
            gf__header__20902 = null;
            Object object7 = gf__body__20903;
            gf__body__20903 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


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

public final class spi$fn__20933$G__20928__20940
extends AFunction {
    Object G__20929;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.log.spi.Scan");

    public spi$fn__20933$G__20928__20940(Object object) {
        this.G__20929 = object;
    }

    public Object invoke(Object gf_____20938, Object gf__opts__20939) {
        Object object;
        spi$fn__20933$G__20928__20940 this_;
        IFn f__8035__auto__20943;
        MethodImplCache cache__8034__auto__20942;
        MethodImplCache methodImplCache = cache__8034__auto__20942 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__20942 = null;
        IFn iFn = f__8035__auto__20943 = methodImplCache.fnFor(Util.classOf((Object)gf_____20938));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__20943;
            f__8035__auto__20943 = null;
            Object object2 = gf_____20938;
            gf_____20938 = null;
            Object object3 = gf__opts__20939;
            gf__opts__20939 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20938, const__1, this_.G__20929);
            Object object4 = gf_____20938;
            gf_____20938 = null;
            Object object5 = gf__opts__20939;
            gf__opts__20939 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


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

public final class spi$fn__20399$G__20392__20408
extends AFunction {
    Object G__20393;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.atom.spi.DurableAtom");

    public spi$fn__20399$G__20392__20408(Object object) {
        this.G__20393 = object;
    }

    public Object invoke(Object gf_____20405, Object gf__f__20406, Object gf__ch__20407) {
        Object object;
        spi$fn__20399$G__20392__20408 this_;
        IFn f__8035__auto__20411;
        MethodImplCache cache__8034__auto__20410;
        MethodImplCache methodImplCache = cache__8034__auto__20410 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__20410 = null;
        IFn iFn = f__8035__auto__20411 = methodImplCache.fnFor(Util.classOf((Object)gf_____20405));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__20411;
            f__8035__auto__20411 = null;
            Object object2 = gf_____20405;
            gf_____20405 = null;
            Object object3 = gf__f__20406;
            gf__f__20406 = null;
            Object object4 = gf__ch__20407;
            gf__ch__20407 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20405, const__1, this_.G__20393);
            Object object5 = gf_____20405;
            gf_____20405 = null;
            Object object6 = gf__f__20406;
            gf__f__20406 = null;
            Object object7 = gf__ch__20407;
            gf__ch__20407 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


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

public final class spi$fn__20414$G__20394__20421
extends AFunction {
    Object G__20395;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.core2.atom.spi.DurableAtom");

    public spi$fn__20414$G__20394__20421(Object object) {
        this.G__20395 = object;
    }

    public Object invoke(Object gf_____20419, Object gf__ch__20420) {
        Object object;
        spi$fn__20414$G__20394__20421 this_;
        IFn f__8035__auto__20424;
        MethodImplCache cache__8034__auto__20423;
        MethodImplCache methodImplCache = cache__8034__auto__20423 = ((AFunction)this_).__methodImplCache;
        cache__8034__auto__20423 = null;
        IFn iFn = f__8035__auto__20424 = methodImplCache.fnFor(Util.classOf((Object)gf_____20419));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__8035__auto__20424;
            f__8035__auto__20424 = null;
            Object object2 = gf_____20419;
            gf_____20419 = null;
            Object object3 = gf__ch__20420;
            gf__ch__20420 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____20419, const__1, this_.G__20395);
            Object object4 = gf_____20419;
            gf_____20419 = null;
            Object object5 = gf__ch__20420;
            gf__ch__20420 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


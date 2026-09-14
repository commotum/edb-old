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

public final class io$fn__9278$G__9273__9283
extends AFunction {
    Object G__9274;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.io.ByteSource");

    public io$fn__9278$G__9273__9283(Object object) {
        this.G__9274 = object;
    }

    public Object invoke(Object gf_____9282) {
        Object object;
        io$fn__9278$G__9273__9283 this_;
        IFn f__7644__auto__9286;
        MethodImplCache cache__7643__auto__9285;
        MethodImplCache methodImplCache = cache__7643__auto__9285 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__9285 = null;
        IFn iFn = f__7644__auto__9286 = methodImplCache.fnFor(Util.classOf((Object)gf_____9282));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__9286;
            f__7644__auto__9286 = null;
            Object object2 = gf_____9282;
            gf_____9282 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____9282, const__1, this_.G__9274);
            Object object3 = gf_____9282;
            gf_____9282 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


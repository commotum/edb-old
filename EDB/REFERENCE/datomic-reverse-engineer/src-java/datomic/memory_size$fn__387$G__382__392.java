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

public final class memory_size$fn__387$G__382__392
extends AFunction {
    Object G__383;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.memory_size.MemorySize");

    public memory_size$fn__387$G__382__392(Object object) {
        this.G__383 = object;
    }

    public Object invoke(Object gf_____391) {
        Object object;
        memory_size$fn__387$G__382__392 this_;
        IFn f__7644__auto__395;
        MethodImplCache cache__7643__auto__394;
        MethodImplCache methodImplCache = cache__7643__auto__394 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__394 = null;
        IFn iFn = f__7644__auto__395 = methodImplCache.fnFor(Util.classOf((Object)gf_____391));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__395;
            f__7644__auto__395 = null;
            Object object2 = gf_____391;
            gf_____391 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____391, const__1, this_.G__383);
            Object object3 = gf_____391;
            gf_____391 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


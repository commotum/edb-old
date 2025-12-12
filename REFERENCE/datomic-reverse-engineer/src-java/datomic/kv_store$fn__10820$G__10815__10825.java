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

public final class kv_store$fn__10820$G__10815__10825
extends AFunction {
    Object G__10816;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.kv_store.Retryable");

    public kv_store$fn__10820$G__10815__10825(Object object) {
        this.G__10816 = object;
    }

    public Object invoke(Object gf_____10824) {
        Object object;
        kv_store$fn__10820$G__10815__10825 this_;
        IFn f__7644__auto__10828;
        MethodImplCache cache__7643__auto__10827;
        MethodImplCache methodImplCache = cache__7643__auto__10827 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10827 = null;
        IFn iFn = f__7644__auto__10828 = methodImplCache.fnFor(Util.classOf((Object)gf_____10824));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10828;
            f__7644__auto__10828 = null;
            Object object2 = gf_____10824;
            gf_____10824 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____10824, const__1, this_.G__10816);
            Object object3 = gf_____10824;
            gf_____10824 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


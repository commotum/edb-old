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

public final class kv_store$fn__10791$G__10750__10798
extends AFunction {
    Object G__10751;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.kv_store.KVStore");

    public kv_store$fn__10791$G__10750__10798(Object object) {
        this.G__10751 = object;
    }

    public Object invoke(Object gf_____10796, Object gf__val_map__10797) {
        Object object;
        kv_store$fn__10791$G__10750__10798 this_;
        IFn f__7644__auto__10801;
        MethodImplCache cache__7643__auto__10800;
        MethodImplCache methodImplCache = cache__7643__auto__10800 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10800 = null;
        IFn iFn = f__7644__auto__10801 = methodImplCache.fnFor(Util.classOf((Object)gf_____10796));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10801;
            f__7644__auto__10801 = null;
            Object object2 = gf_____10796;
            gf_____10796 = null;
            Object object3 = gf__val_map__10797;
            gf__val_map__10797 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____10796, const__1, this_.G__10751);
            Object object4 = gf_____10796;
            gf_____10796 = null;
            Object object5 = gf__val_map__10797;
            gf__val_map__10797 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


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

public final class simple_kv$fn__16701$G__16681__16708
extends AFunction {
    Object G__16682;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.simple_kv.KV");

    public simple_kv$fn__16701$G__16681__16708(Object object) {
        this.G__16682 = object;
    }

    public Object invoke(Object gf_____16706, Object gf__key__16707) {
        Object object;
        simple_kv$fn__16701$G__16681__16708 this_;
        IFn f__7644__auto__16711;
        MethodImplCache cache__7643__auto__16710;
        MethodImplCache methodImplCache = cache__7643__auto__16710 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16710 = null;
        IFn iFn = f__7644__auto__16711 = methodImplCache.fnFor(Util.classOf((Object)gf_____16706));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16711;
            f__7644__auto__16711 = null;
            Object object2 = gf_____16706;
            gf_____16706 = null;
            Object object3 = gf__key__16707;
            gf__key__16707 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____16706, const__1, this_.G__16682);
            Object object4 = gf_____16706;
            gf_____16706 = null;
            Object object5 = gf__key__16707;
            gf__key__16707 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


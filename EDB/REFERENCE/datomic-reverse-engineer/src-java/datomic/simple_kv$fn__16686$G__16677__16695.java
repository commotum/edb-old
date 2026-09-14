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

public final class simple_kv$fn__16686$G__16677__16695
extends AFunction {
    Object G__16678;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.simple_kv.KV");

    public simple_kv$fn__16686$G__16677__16695(Object object) {
        this.G__16678 = object;
    }

    public Object invoke(Object gf_____16692, Object gf__key__16693, Object gf__val__16694) {
        Object object;
        simple_kv$fn__16686$G__16677__16695 this_;
        IFn f__7644__auto__16698;
        MethodImplCache cache__7643__auto__16697;
        MethodImplCache methodImplCache = cache__7643__auto__16697 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16697 = null;
        IFn iFn = f__7644__auto__16698 = methodImplCache.fnFor(Util.classOf((Object)gf_____16692));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16698;
            f__7644__auto__16698 = null;
            Object object2 = gf_____16692;
            gf_____16692 = null;
            Object object3 = gf__key__16693;
            gf__key__16693 = null;
            Object object4 = gf__val__16694;
            gf__val__16694 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____16692, const__1, this_.G__16678);
            Object object5 = gf_____16692;
            gf_____16692 = null;
            Object object6 = gf__key__16693;
            gf__key__16693 = null;
            Object object7 = gf__val__16694;
            gf__val__16694 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


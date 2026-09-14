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

public final class kv_store$fn__10761$G__10754__10770
extends AFunction {
    Object G__10755;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.kv_store.KVStore");

    public kv_store$fn__10761$G__10754__10770(Object object) {
        this.G__10755 = object;
    }

    public Object invoke(Object gf_____10767, Object gf__key__10768, Object gf__consistent_QMARK___10769) {
        Object object;
        kv_store$fn__10761$G__10754__10770 this_;
        IFn f__7644__auto__10773;
        MethodImplCache cache__7643__auto__10772;
        MethodImplCache methodImplCache = cache__7643__auto__10772 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10772 = null;
        IFn iFn = f__7644__auto__10773 = methodImplCache.fnFor(Util.classOf((Object)gf_____10767));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10773;
            f__7644__auto__10773 = null;
            Object object2 = gf_____10767;
            gf_____10767 = null;
            Object object3 = gf__key__10768;
            gf__key__10768 = null;
            Object object4 = gf__consistent_QMARK___10769;
            gf__consistent_QMARK___10769 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____10767, const__1, this_.G__10755);
            Object object5 = gf_____10767;
            gf_____10767 = null;
            Object object6 = gf__key__10768;
            gf__key__10768 = null;
            Object object7 = gf__consistent_QMARK___10769;
            gf__consistent_QMARK___10769 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


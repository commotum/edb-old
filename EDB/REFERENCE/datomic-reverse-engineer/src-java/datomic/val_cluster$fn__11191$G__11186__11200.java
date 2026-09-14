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

public final class val_cluster$fn__11191$G__11186__11200
extends AFunction {
    Object G__11187;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.val_cluster.Impl");

    public val_cluster$fn__11191$G__11186__11200(Object object) {
        this.G__11187 = object;
    }

    public Object invoke(Object gf_____11197, Object gf__val_key__11198, Object gf__opts__11199) {
        Object object;
        val_cluster$fn__11191$G__11186__11200 this_;
        IFn f__7644__auto__11203;
        MethodImplCache cache__7643__auto__11202;
        MethodImplCache methodImplCache = cache__7643__auto__11202 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__11202 = null;
        IFn iFn = f__7644__auto__11203 = methodImplCache.fnFor(Util.classOf((Object)gf_____11197));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__11203;
            f__7644__auto__11203 = null;
            Object object2 = gf_____11197;
            gf_____11197 = null;
            Object object3 = gf__val_key__11198;
            gf__val_key__11198 = null;
            Object object4 = gf__opts__11199;
            gf__opts__11199 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____11197, const__1, this_.G__11187);
            Object object5 = gf_____11197;
            gf_____11197 = null;
            Object object6 = gf__val_key__11198;
            gf__val_key__11198 = null;
            Object object7 = gf__opts__11199;
            gf__opts__11199 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


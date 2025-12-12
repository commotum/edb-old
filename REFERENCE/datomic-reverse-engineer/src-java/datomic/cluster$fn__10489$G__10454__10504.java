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

public final class cluster$fn__10489$G__10454__10504
extends AFunction {
    Object G__10455;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.ClusteredStore");

    public cluster$fn__10489$G__10454__10504(Object object) {
        this.G__10455 = object;
    }

    public Object invoke(Object gf__cs__10498, Object gf__pod_key__10499, Object gf__rev__10500, Object gf__etag__10501, Object gf__buf__10502, Object gf__metamap__10503) {
        Object object;
        cluster$fn__10489$G__10454__10504 this_;
        IFn f__7644__auto__10507;
        MethodImplCache cache__7643__auto__10506;
        MethodImplCache methodImplCache = cache__7643__auto__10506 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10506 = null;
        IFn iFn = f__7644__auto__10507 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10498));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10507;
            f__7644__auto__10507 = null;
            Object object2 = gf__cs__10498;
            gf__cs__10498 = null;
            Object object3 = gf__pod_key__10499;
            gf__pod_key__10499 = null;
            Object object4 = gf__rev__10500;
            gf__rev__10500 = null;
            Object object5 = gf__etag__10501;
            gf__etag__10501 = null;
            Object object6 = gf__buf__10502;
            gf__buf__10502 = null;
            Object object7 = gf__metamap__10503;
            gf__metamap__10503 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5, object6, object7);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10498, const__1, this_.G__10455);
            Object object8 = gf__cs__10498;
            gf__cs__10498 = null;
            Object object9 = gf__pod_key__10499;
            gf__pod_key__10499 = null;
            Object object10 = gf__rev__10500;
            gf__rev__10500 = null;
            Object object11 = gf__etag__10501;
            gf__etag__10501 = null;
            Object object12 = gf__buf__10502;
            gf__buf__10502 = null;
            Object object13 = gf__metamap__10503;
            gf__metamap__10503 = null;
            this_ = null;
            object = iFn3.invoke(object8, object9, object10, object11, object12, object13);
        }
        return object;
    }
}


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

public final class external_sort$fn__14385$G__14356__14392
extends AFunction {
    Object G__14357;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.external_sort.IO");

    public external_sort$fn__14385$G__14356__14392(Object object) {
        this.G__14357 = object;
    }

    public Object invoke(Object gf_____14390, Object gf__fname__14391) {
        Object object;
        external_sort$fn__14385$G__14356__14392 this_;
        IFn f__7644__auto__14395;
        MethodImplCache cache__7643__auto__14394;
        MethodImplCache methodImplCache = cache__7643__auto__14394 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14394 = null;
        IFn iFn = f__7644__auto__14395 = methodImplCache.fnFor(Util.classOf((Object)gf_____14390));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14395;
            f__7644__auto__14395 = null;
            Object object2 = gf_____14390;
            gf_____14390 = null;
            Object object3 = gf__fname__14391;
            gf__fname__14391 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14390, const__1, this_.G__14357);
            Object object4 = gf_____14390;
            gf_____14390 = null;
            Object object5 = gf__fname__14391;
            gf__fname__14391 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


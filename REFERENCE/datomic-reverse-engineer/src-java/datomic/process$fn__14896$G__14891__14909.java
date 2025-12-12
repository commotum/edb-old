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

public final class process$fn__14896$G__14891__14909
extends AFunction {
    Object G__14892;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.process.CriticalFailure");

    public process$fn__14896$G__14891__14909(Object object) {
        this.G__14892 = object;
    }

    public Object invoke(Object gf_____14906, Object gf__msg__14907, Object gf__t__14908) {
        Object object;
        process$fn__14896$G__14891__14909 this_;
        IFn f__7644__auto__14912;
        MethodImplCache cache__7643__auto__14911;
        MethodImplCache methodImplCache = cache__7643__auto__14911 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14911 = null;
        IFn iFn = f__7644__auto__14912 = methodImplCache.fnFor(Util.classOf((Object)gf_____14906));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14912;
            f__7644__auto__14912 = null;
            Object object2 = gf_____14906;
            gf_____14906 = null;
            Object object3 = gf__msg__14907;
            gf__msg__14907 = null;
            Object object4 = gf__t__14908;
            gf__t__14908 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14906, const__1, this_.G__14892);
            Object object5 = gf_____14906;
            gf_____14906 = null;
            Object object6 = gf__msg__14907;
            gf__msg__14907 = null;
            Object object7 = gf__t__14908;
            gf__t__14908 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }

    public Object invoke(Object gf_____14904, Object gf__msg__14905) {
        Object object;
        process$fn__14896$G__14891__14909 this_;
        IFn f__7644__auto__14914;
        MethodImplCache cache__7643__auto__14913;
        MethodImplCache methodImplCache = cache__7643__auto__14913 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14913 = null;
        IFn iFn = f__7644__auto__14914 = methodImplCache.fnFor(Util.classOf((Object)gf_____14904));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14914;
            f__7644__auto__14914 = null;
            Object object2 = gf_____14904;
            gf_____14904 = null;
            Object object3 = gf__msg__14905;
            gf__msg__14905 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____14904, const__1, this_.G__14892);
            Object object4 = gf_____14904;
            gf_____14904 = null;
            Object object5 = gf__msg__14905;
            gf__msg__14905 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


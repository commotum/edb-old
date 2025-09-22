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

public final class datalog$fn__18083$G__18057__18098
extends AFunction {
    Object G__18058;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.datalog.IJoin");

    public datalog$fn__18083$G__18057__18098(Object object) {
        this.G__18058 = object;
    }

    public Object invoke(Object gf__ys__18092, Object gf__xs__18093, Object gf__join_map__18094, Object gf__project_map_x__18095, Object gf__project_map_y__18096, Object gf__predctor__18097) {
        Object object;
        datalog$fn__18083$G__18057__18098 this_;
        IFn f__7644__auto__18101;
        MethodImplCache cache__7643__auto__18100;
        MethodImplCache methodImplCache = cache__7643__auto__18100 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__18100 = null;
        IFn iFn = f__7644__auto__18101 = methodImplCache.fnFor(Util.classOf((Object)gf__ys__18092));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__18101;
            f__7644__auto__18101 = null;
            Object object2 = gf__ys__18092;
            gf__ys__18092 = null;
            Object object3 = gf__xs__18093;
            gf__xs__18093 = null;
            Object object4 = gf__join_map__18094;
            gf__join_map__18094 = null;
            Object object5 = gf__project_map_x__18095;
            gf__project_map_x__18095 = null;
            Object object6 = gf__project_map_y__18096;
            gf__project_map_y__18096 = null;
            Object object7 = gf__predctor__18097;
            gf__predctor__18097 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5, object6, object7);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__ys__18092, const__1, this_.G__18058);
            Object object8 = gf__ys__18092;
            gf__ys__18092 = null;
            Object object9 = gf__xs__18093;
            gf__xs__18093 = null;
            Object object10 = gf__join_map__18094;
            gf__join_map__18094 = null;
            Object object11 = gf__project_map_x__18095;
            gf__project_map_x__18095 = null;
            Object object12 = gf__project_map_y__18096;
            gf__project_map_y__18096 = null;
            Object object13 = gf__predctor__18097;
            gf__predctor__18097 = null;
            this_ = null;
            object = iFn3.invoke(object8, object9, object10, object11, object12, object13);
        }
        return object;
    }
}


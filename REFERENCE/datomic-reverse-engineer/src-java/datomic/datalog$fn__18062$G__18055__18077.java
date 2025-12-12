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

public final class datalog$fn__18062$G__18055__18077
extends AFunction {
    Object G__18056;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.datalog.IJoin");

    public datalog$fn__18062$G__18055__18077(Object object) {
        this.G__18056 = object;
    }

    public Object invoke(Object gf__xs__18071, Object gf__ys__18072, Object gf__join_map__18073, Object gf__project_map_x__18074, Object gf__project_map_y__18075, Object gf__predctor__18076) {
        Object object;
        datalog$fn__18062$G__18055__18077 this_;
        IFn f__7644__auto__18080;
        MethodImplCache cache__7643__auto__18079;
        MethodImplCache methodImplCache = cache__7643__auto__18079 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__18079 = null;
        IFn iFn = f__7644__auto__18080 = methodImplCache.fnFor(Util.classOf((Object)gf__xs__18071));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__18080;
            f__7644__auto__18080 = null;
            Object object2 = gf__xs__18071;
            gf__xs__18071 = null;
            Object object3 = gf__ys__18072;
            gf__ys__18072 = null;
            Object object4 = gf__join_map__18073;
            gf__join_map__18073 = null;
            Object object5 = gf__project_map_x__18074;
            gf__project_map_x__18074 = null;
            Object object6 = gf__project_map_y__18075;
            gf__project_map_y__18075 = null;
            Object object7 = gf__predctor__18076;
            gf__predctor__18076 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5, object6, object7);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__xs__18071, const__1, this_.G__18056);
            Object object8 = gf__xs__18071;
            gf__xs__18071 = null;
            Object object9 = gf__ys__18072;
            gf__ys__18072 = null;
            Object object10 = gf__join_map__18073;
            gf__join_map__18073 = null;
            Object object11 = gf__project_map_x__18074;
            gf__project_map_x__18074 = null;
            Object object12 = gf__project_map_y__18075;
            gf__project_map_y__18075 = null;
            Object object13 = gf__predctor__18076;
            gf__predctor__18076 = null;
            this_ = null;
            object = iFn3.invoke(object8, object9, object10, object11, object12, object13);
        }
        return object;
    }
}


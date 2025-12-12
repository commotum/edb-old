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

public final class datalog$fn__18185$G__18180__18196
extends AFunction {
    Object G__18181;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.datalog.ExtRel");

    public datalog$fn__18185$G__18180__18196(Object object) {
        this.G__18181 = object;
    }

    public Object invoke(Object gf__src__18192, Object gf__consts__18193, Object gf__starts__18194, Object gf__whiles__18195) {
        Object object;
        datalog$fn__18185$G__18180__18196 this_;
        IFn f__7644__auto__18199;
        MethodImplCache cache__7643__auto__18198;
        MethodImplCache methodImplCache = cache__7643__auto__18198 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__18198 = null;
        IFn iFn = f__7644__auto__18199 = methodImplCache.fnFor(Util.classOf((Object)gf__src__18192));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__18199;
            f__7644__auto__18199 = null;
            Object object2 = gf__src__18192;
            gf__src__18192 = null;
            Object object3 = gf__consts__18193;
            gf__consts__18193 = null;
            Object object4 = gf__starts__18194;
            gf__starts__18194 = null;
            Object object5 = gf__whiles__18195;
            gf__whiles__18195 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__src__18192, const__1, this_.G__18181);
            Object object6 = gf__src__18192;
            gf__src__18192 = null;
            Object object7 = gf__consts__18193;
            gf__consts__18193 = null;
            Object object8 = gf__starts__18194;
            gf__starts__18194 = null;
            Object object9 = gf__whiles__18195;
            gf__whiles__18195 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}


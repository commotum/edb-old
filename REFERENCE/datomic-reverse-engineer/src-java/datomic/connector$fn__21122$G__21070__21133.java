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

public final class connector$fn__21122$G__21070__21133
extends AFunction {
    Object G__21071;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.connector.TransactorConnector");

    public connector$fn__21122$G__21070__21133(Object object) {
        this.G__21071 = object;
    }

    public Object invoke(Object gf_____21129, Object gf__request__21130, Object gf__arg__21131, Object gf__timeout_msec__21132) {
        Object object;
        connector$fn__21122$G__21070__21133 this_;
        IFn f__7644__auto__21136;
        MethodImplCache cache__7643__auto__21135;
        MethodImplCache methodImplCache = cache__7643__auto__21135 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21135 = null;
        IFn iFn = f__7644__auto__21136 = methodImplCache.fnFor(Util.classOf((Object)gf_____21129));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21136;
            f__7644__auto__21136 = null;
            Object object2 = gf_____21129;
            gf_____21129 = null;
            Object object3 = gf__request__21130;
            gf__request__21130 = null;
            Object object4 = gf__arg__21131;
            gf__arg__21131 = null;
            Object object5 = gf__timeout_msec__21132;
            gf__timeout_msec__21132 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21129, const__1, this_.G__21071);
            Object object6 = gf_____21129;
            gf_____21129 = null;
            Object object7 = gf__request__21130;
            gf__request__21130 = null;
            Object object8 = gf__arg__21131;
            gf__arg__21131 = null;
            Object object9 = gf__timeout_msec__21132;
            gf__timeout_msec__21132 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }
}


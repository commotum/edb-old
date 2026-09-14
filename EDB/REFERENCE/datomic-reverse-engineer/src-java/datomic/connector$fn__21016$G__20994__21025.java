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

public final class connector$fn__21016$G__20994__21025
extends AFunction {
    Object G__20995;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.connector.NotificationHandler");

    public connector$fn__21016$G__20994__21025(Object object) {
        this.G__20995 = object;
    }

    public Object invoke(Object gf_____21022, Object gf__id__21023, Object gf__error__21024) {
        Object object;
        connector$fn__21016$G__20994__21025 this_;
        IFn f__7644__auto__21028;
        MethodImplCache cache__7643__auto__21027;
        MethodImplCache methodImplCache = cache__7643__auto__21027 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21027 = null;
        IFn iFn = f__7644__auto__21028 = methodImplCache.fnFor(Util.classOf((Object)gf_____21022));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21028;
            f__7644__auto__21028 = null;
            Object object2 = gf_____21022;
            gf_____21022 = null;
            Object object3 = gf__id__21023;
            gf__id__21023 = null;
            Object object4 = gf__error__21024;
            gf__error__21024 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21022, const__1, this_.G__20995);
            Object object5 = gf_____21022;
            gf_____21022 = null;
            Object object6 = gf__id__21023;
            gf__id__21023 = null;
            Object object7 = gf__error__21024;
            gf__error__21024 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


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

public final class connector$fn__21031$G__20998__21036
extends AFunction {
    Object G__20999;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.connector.NotificationHandler");

    public connector$fn__21031$G__20998__21036(Object object) {
        this.G__20999 = object;
    }

    public Object invoke(Object gf_____21035) {
        Object object;
        connector$fn__21031$G__20998__21036 this_;
        IFn f__7644__auto__21039;
        MethodImplCache cache__7643__auto__21038;
        MethodImplCache methodImplCache = cache__7643__auto__21038 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__21038 = null;
        IFn iFn = f__7644__auto__21039 = methodImplCache.fnFor(Util.classOf((Object)gf_____21035));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__21039;
            f__7644__auto__21039 = null;
            Object object2 = gf_____21035;
            gf_____21035 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____21035, const__1, this_.G__20999);
            Object object3 = gf_____21035;
            gf_____21035 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


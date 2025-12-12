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

public final class common$fn__9077$G__9072__9082
extends AFunction {
    Object G__9073;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.common.AsyncShutdown");

    public common$fn__9077$G__9072__9082(Object object) {
        this.G__9073 = object;
    }

    public Object invoke(Object gf__o__9081) {
        Object object;
        common$fn__9077$G__9072__9082 this_;
        IFn f__7644__auto__9085;
        MethodImplCache cache__7643__auto__9084;
        MethodImplCache methodImplCache = cache__7643__auto__9084 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__9084 = null;
        IFn iFn = f__7644__auto__9085 = methodImplCache.fnFor(Util.classOf((Object)gf__o__9081));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__9085;
            f__7644__auto__9085 = null;
            Object object2 = gf__o__9081;
            gf__o__9081 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__o__9081, const__1, this_.G__9073);
            Object object3 = gf__o__9081;
            gf__o__9081 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


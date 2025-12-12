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

public final class log$fn__16019$G__15988__16024
extends AFunction {
    Object G__15989;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.log.Log");

    public log$fn__16019$G__15988__16024(Object object) {
        this.G__15989 = object;
    }

    public Object invoke(Object gf__log__16023) {
        Object object;
        log$fn__16019$G__15988__16024 this_;
        IFn f__7644__auto__16027;
        MethodImplCache cache__7643__auto__16026;
        MethodImplCache methodImplCache = cache__7643__auto__16026 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16026 = null;
        IFn iFn = f__7644__auto__16027 = methodImplCache.fnFor(Util.classOf((Object)gf__log__16023));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16027;
            f__7644__auto__16027 = null;
            Object object2 = gf__log__16023;
            gf__log__16023 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__log__16023, const__1, this_.G__15989);
            Object object3 = gf__log__16023;
            gf__log__16023 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


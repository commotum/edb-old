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

public final class log$fn__16058$G__15978__16065
extends AFunction {
    Object G__15979;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.log.Log");

    public log$fn__16058$G__15978__16065(Object object) {
        this.G__15979 = object;
    }

    public Object invoke(Object gf__log__16063, Object gf__cs__16064) {
        Object object;
        log$fn__16058$G__15978__16065 this_;
        IFn f__7644__auto__16068;
        MethodImplCache cache__7643__auto__16067;
        MethodImplCache methodImplCache = cache__7643__auto__16067 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16067 = null;
        IFn iFn = f__7644__auto__16068 = methodImplCache.fnFor(Util.classOf((Object)gf__log__16063));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16068;
            f__7644__auto__16068 = null;
            Object object2 = gf__log__16063;
            gf__log__16063 = null;
            Object object3 = gf__cs__16064;
            gf__cs__16064 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__log__16063, const__1, this_.G__15979);
            Object object4 = gf__log__16063;
            gf__log__16063 = null;
            Object object5 = gf__cs__16064;
            gf__cs__16064 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


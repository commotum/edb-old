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

public final class log$fn__16092$G__16087__16097
extends AFunction {
    Object G__16088;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.log.LogKey");

    public log$fn__16092$G__16087__16097(Object object) {
        this.G__16088 = object;
    }

    public Object invoke(Object gf_____16096) {
        Object object;
        log$fn__16092$G__16087__16097 this_;
        IFn f__7644__auto__16100;
        MethodImplCache cache__7643__auto__16099;
        MethodImplCache methodImplCache = cache__7643__auto__16099 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16099 = null;
        IFn iFn = f__7644__auto__16100 = methodImplCache.fnFor(Util.classOf((Object)gf_____16096));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16100;
            f__7644__auto__16100 = null;
            Object object2 = gf_____16096;
            gf_____16096 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____16096, const__1, this_.G__16088);
            Object object3 = gf_____16096;
            gf_____16096 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


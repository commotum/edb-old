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

public final class queue$fn__12107$G__12102__12112
extends AFunction {
    Object G__12103;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.queue.Clear");

    public queue$fn__12107$G__12102__12112(Object object) {
        this.G__12103 = object;
    }

    public Object invoke(Object gf__q__12111) {
        Object object;
        queue$fn__12107$G__12102__12112 this_;
        IFn f__7644__auto__12115;
        MethodImplCache cache__7643__auto__12114;
        MethodImplCache methodImplCache = cache__7643__auto__12114 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12114 = null;
        IFn iFn = f__7644__auto__12115 = methodImplCache.fnFor(Util.classOf((Object)gf__q__12111));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12115;
            f__7644__auto__12115 = null;
            Object object2 = gf__q__12111;
            gf__q__12111 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__q__12111, const__1, this_.G__12103);
            Object object3 = gf__q__12111;
            gf__q__12111 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


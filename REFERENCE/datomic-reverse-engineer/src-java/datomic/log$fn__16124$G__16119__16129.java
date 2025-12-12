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

public final class log$fn__16124$G__16119__16129
extends AFunction {
    Object G__16120;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.log.LogDirSeq");

    public log$fn__16124$G__16119__16129(Object object) {
        this.G__16120 = object;
    }

    public Object invoke(Object gf_____16128) {
        Object object;
        log$fn__16124$G__16119__16129 this_;
        IFn f__7644__auto__16132;
        MethodImplCache cache__7643__auto__16131;
        MethodImplCache methodImplCache = cache__7643__auto__16131 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__16131 = null;
        IFn iFn = f__7644__auto__16132 = methodImplCache.fnFor(Util.classOf((Object)gf_____16128));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__16132;
            f__7644__auto__16132 = null;
            Object object2 = gf_____16128;
            gf_____16128 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf_____16128, const__1, this_.G__16120);
            Object object3 = gf_____16128;
            gf_____16128 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


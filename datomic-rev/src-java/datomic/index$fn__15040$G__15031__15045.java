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

public final class index$fn__15040$G__15031__15045
extends AFunction {
    Object G__15032;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.index.ITreeIter");

    public index$fn__15040$G__15031__15045(Object object) {
        this.G__15032 = object;
    }

    public Object invoke(Object gf__iter__15044) {
        Object object;
        index$fn__15040$G__15031__15045 this_;
        IFn f__7644__auto__15048;
        MethodImplCache cache__7643__auto__15047;
        MethodImplCache methodImplCache = cache__7643__auto__15047 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__15047 = null;
        IFn iFn = f__7644__auto__15048 = methodImplCache.fnFor(Util.classOf((Object)gf__iter__15044));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__15048;
            f__7644__auto__15048 = null;
            Object object2 = gf__iter__15044;
            gf__iter__15044 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__iter__15044, const__1, this_.G__15032);
            Object object3 = gf__iter__15044;
            gf__iter__15044 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


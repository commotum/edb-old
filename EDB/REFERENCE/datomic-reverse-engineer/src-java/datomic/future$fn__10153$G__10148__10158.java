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

public final class future$fn__10153$G__10148__10158
extends AFunction {
    Object G__10149;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.future.GetChannel");

    public future$fn__10153$G__10148__10158(Object object) {
        this.G__10149 = object;
    }

    public Object invoke(Object gf__fut__10157) {
        Object object;
        future$fn__10153$G__10148__10158 this_;
        IFn f__7644__auto__10161;
        MethodImplCache cache__7643__auto__10160;
        MethodImplCache methodImplCache = cache__7643__auto__10160 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10160 = null;
        IFn iFn = f__7644__auto__10161 = methodImplCache.fnFor(Util.classOf((Object)gf__fut__10157));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10161;
            f__7644__auto__10161 = null;
            Object object2 = gf__fut__10157;
            gf__fut__10157 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__fut__10157, const__1, this_.G__10149);
            Object object3 = gf__fut__10157;
            gf__fut__10157 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


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

public final class index$fn__15078$G__15073__15085
extends AFunction {
    Object G__15074;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.index.IIndex");

    public index$fn__15078$G__15073__15085(Object object) {
        this.G__15074 = object;
    }

    public Object invoke(Object gf__idx__15083, Object gf__k__15084) {
        Object object;
        index$fn__15078$G__15073__15085 this_;
        IFn f__7644__auto__15088;
        MethodImplCache cache__7643__auto__15087;
        MethodImplCache methodImplCache = cache__7643__auto__15087 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__15087 = null;
        IFn iFn = f__7644__auto__15088 = methodImplCache.fnFor(Util.classOf((Object)gf__idx__15083));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__15088;
            f__7644__auto__15088 = null;
            Object object2 = gf__idx__15083;
            gf__idx__15083 = null;
            Object object3 = gf__k__15084;
            gf__k__15084 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__idx__15083, const__1, this_.G__15074);
            Object object4 = gf__idx__15083;
            gf__idx__15083 = null;
            Object object5 = gf__k__15084;
            gf__k__15084 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


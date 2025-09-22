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

public final class datafy$fn__17223$G__17218__17228
extends AFunction {
    Object G__17219;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.datafy.ObjectToData");

    public datafy$fn__17223$G__17218__17228(Object object) {
        this.G__17219 = object;
    }

    public Object invoke(Object gf__o__17227) {
        Object object;
        datafy$fn__17223$G__17218__17228 this_;
        IFn f__7644__auto__17231;
        MethodImplCache cache__7643__auto__17230;
        MethodImplCache methodImplCache = cache__7643__auto__17230 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__17230 = null;
        IFn iFn = f__7644__auto__17231 = methodImplCache.fnFor(Util.classOf((Object)gf__o__17227));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__17231;
            f__7644__auto__17231 = null;
            Object object2 = gf__o__17227;
            gf__o__17227 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__o__17227, const__1, this_.G__17219);
            Object object3 = gf__o__17227;
            gf__o__17227 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


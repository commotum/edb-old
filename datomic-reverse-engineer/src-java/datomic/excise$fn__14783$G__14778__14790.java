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

public final class excise$fn__14783$G__14778__14790
extends AFunction {
    Object G__14779;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.excise.ExcisePred");

    public excise$fn__14783$G__14778__14790(Object object) {
        this.G__14779 = object;
    }

    public Object invoke(Object gf__epred__14788, Object gf__datom__14789) {
        Object object;
        excise$fn__14783$G__14778__14790 this_;
        IFn f__7644__auto__14793;
        MethodImplCache cache__7643__auto__14792;
        MethodImplCache methodImplCache = cache__7643__auto__14792 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14792 = null;
        IFn iFn = f__7644__auto__14793 = methodImplCache.fnFor(Util.classOf((Object)gf__epred__14788));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14793;
            f__7644__auto__14793 = null;
            Object object2 = gf__epred__14788;
            gf__epred__14788 = null;
            Object object3 = gf__datom__14789;
            gf__datom__14789 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__epred__14788, const__1, this_.G__14779);
            Object object4 = gf__epred__14788;
            gf__epred__14788 = null;
            Object object5 = gf__datom__14789;
            gf__datom__14789 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


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

public final class excise$fn__14796$G__14776__14801
extends AFunction {
    Object G__14777;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.excise.ExcisePred");

    public excise$fn__14796$G__14776__14801(Object object) {
        this.G__14777 = object;
    }

    public Object invoke(Object gf__epred__14800) {
        Object object;
        excise$fn__14796$G__14776__14801 this_;
        IFn f__7644__auto__14804;
        MethodImplCache cache__7643__auto__14803;
        MethodImplCache methodImplCache = cache__7643__auto__14803 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__14803 = null;
        IFn iFn = f__7644__auto__14804 = methodImplCache.fnFor(Util.classOf((Object)gf__epred__14800));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__14804;
            f__7644__auto__14804 = null;
            Object object2 = gf__epred__14800;
            gf__epred__14800 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__epred__14800, const__1, this_.G__14777);
            Object object3 = gf__epred__14800;
            gf__epred__14800 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


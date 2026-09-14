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

public final class cluster$fn__10732$G__10727__10737
extends AFunction {
    Object G__10728;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.RefClusterStore");

    public cluster$fn__10732$G__10727__10737(Object object) {
        this.G__10728 = object;
    }

    public Object invoke(Object gf__cs__10736) {
        Object object;
        cluster$fn__10732$G__10727__10737 this_;
        IFn f__7644__auto__10740;
        MethodImplCache cache__7643__auto__10739;
        MethodImplCache methodImplCache = cache__7643__auto__10739 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10739 = null;
        IFn iFn = f__7644__auto__10740 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10736));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10740;
            f__7644__auto__10740 = null;
            Object object2 = gf__cs__10736;
            gf__cs__10736 = null;
            this_ = null;
            object = iFn2.invoke(object2);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10736, const__1, this_.G__10728);
            Object object3 = gf__cs__10736;
            gf__cs__10736 = null;
            this_ = null;
            object = iFn3.invoke(object3);
        }
        return object;
    }
}


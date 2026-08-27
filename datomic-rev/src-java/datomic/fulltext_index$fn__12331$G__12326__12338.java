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

public final class fulltext_index$fn__12331$G__12326__12338
extends AFunction {
    Object G__12327;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.fulltext_index.LuceneProvider");

    public fulltext_index$fn__12331$G__12326__12338(Object object) {
        this.G__12327 = object;
    }

    public Object invoke(Object gf__this__12336, Object gf__attr__12337) {
        Object object;
        fulltext_index$fn__12331$G__12326__12338 this_;
        IFn f__7644__auto__12341;
        MethodImplCache cache__7643__auto__12340;
        MethodImplCache methodImplCache = cache__7643__auto__12340 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12340 = null;
        IFn iFn = f__7644__auto__12341 = methodImplCache.fnFor(Util.classOf((Object)gf__this__12336));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12341;
            f__7644__auto__12341 = null;
            Object object2 = gf__this__12336;
            gf__this__12336 = null;
            Object object3 = gf__attr__12337;
            gf__attr__12337 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__this__12336, const__1, this_.G__12327);
            Object object4 = gf__this__12336;
            gf__this__12336 = null;
            Object object5 = gf__attr__12337;
            gf__attr__12337 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


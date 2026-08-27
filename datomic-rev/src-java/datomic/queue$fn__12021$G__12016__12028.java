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

public final class queue$fn__12021$G__12016__12028
extends AFunction {
    Object G__12017;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.queue.Consumer");

    public queue$fn__12021$G__12016__12028(Object object) {
        this.G__12017 = object;
    }

    public Object invoke(Object gf__source__12026, Object gf__or_else__12027) {
        Object object;
        queue$fn__12021$G__12016__12028 this_;
        IFn f__7644__auto__12031;
        MethodImplCache cache__7643__auto__12030;
        MethodImplCache methodImplCache = cache__7643__auto__12030 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__12030 = null;
        IFn iFn = f__7644__auto__12031 = methodImplCache.fnFor(Util.classOf((Object)gf__source__12026));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__12031;
            f__7644__auto__12031 = null;
            Object object2 = gf__source__12026;
            gf__source__12026 = null;
            Object object3 = gf__or_else__12027;
            gf__or_else__12027 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__source__12026, const__1, this_.G__12017);
            Object object4 = gf__source__12026;
            gf__source__12026 = null;
            Object object5 = gf__or_else__12027;
            gf__or_else__12027 = null;
            this_ = null;
            object = iFn3.invoke(object4, object5);
        }
        return object;
    }
}


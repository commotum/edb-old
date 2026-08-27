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

public final class cluster$fn__10549$G__10438__10566
extends AFunction {
    Object G__10439;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"-cache-protocol-fn");
    public static final Object const__1 = RT.classForName((String)"datomic.cluster.ClusteredStore");

    public cluster$fn__10549$G__10438__10566(Object object) {
        this.G__10439 = object;
    }

    public Object invoke(Object gf__cs__10562, Object gf__priority__10563, Object gf__val_key__10564, Object gf__buf__10565) {
        Object object;
        cluster$fn__10549$G__10438__10566 this_;
        IFn f__7644__auto__10569;
        MethodImplCache cache__7643__auto__10568;
        MethodImplCache methodImplCache = cache__7643__auto__10568 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10568 = null;
        IFn iFn = f__7644__auto__10569 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10562));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10569;
            f__7644__auto__10569 = null;
            Object object2 = gf__cs__10562;
            gf__cs__10562 = null;
            Object object3 = gf__priority__10563;
            gf__priority__10563 = null;
            Object object4 = gf__val_key__10564;
            gf__val_key__10564 = null;
            Object object5 = gf__buf__10565;
            gf__buf__10565 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4, object5);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10562, const__1, this_.G__10439);
            Object object6 = gf__cs__10562;
            gf__cs__10562 = null;
            Object object7 = gf__priority__10563;
            gf__priority__10563 = null;
            Object object8 = gf__val_key__10564;
            gf__val_key__10564 = null;
            Object object9 = gf__buf__10565;
            gf__buf__10565 = null;
            this_ = null;
            object = iFn3.invoke(object6, object7, object8, object9);
        }
        return object;
    }

    public Object invoke(Object gf__cs__10559, Object gf__val_key__10560, Object gf__buf__10561) {
        Object object;
        cluster$fn__10549$G__10438__10566 this_;
        IFn f__7644__auto__10571;
        MethodImplCache cache__7643__auto__10570;
        MethodImplCache methodImplCache = cache__7643__auto__10570 = ((AFunction)this_).__methodImplCache;
        cache__7643__auto__10570 = null;
        IFn iFn = f__7644__auto__10571 = methodImplCache.fnFor(Util.classOf((Object)gf__cs__10559));
        if (iFn != null && iFn != Boolean.FALSE) {
            IFn iFn2 = f__7644__auto__10571;
            f__7644__auto__10571 = null;
            Object object2 = gf__cs__10559;
            gf__cs__10559 = null;
            Object object3 = gf__val_key__10560;
            gf__val_key__10560 = null;
            Object object4 = gf__buf__10561;
            gf__buf__10561 = null;
            this_ = null;
            object = iFn2.invoke(object2, object3, object4);
        } else {
            IFn iFn3 = (IFn)((IFn)const__0.getRawRoot()).invoke((Object)this_, gf__cs__10559, const__1, this_.G__10439);
            Object object5 = gf__cs__10559;
            gf__cs__10559 = null;
            Object object6 = gf__val_key__10560;
            gf__val_key__10560 = null;
            Object object7 = gf__buf__10561;
            gf__buf__10561 = null;
            this_ = null;
            object = iFn3.invoke(object5, object6, object7);
        }
        return object;
    }
}


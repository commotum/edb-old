/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.common$coerce_args$fn__9055$fn__9060;

public final class common$coerce_args$fn__9055
extends AFunction {
    Object predfns;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");

    public common$coerce_args$fn__9055(Object object) {
        this.predfns = object;
    }

    public Object invoke(Object m, Object p__9054) {
        Object object;
        common$coerce_args$fn__9055 this_;
        Object temp__5455__auto__9066;
        Object object2 = p__9054;
        p__9054 = null;
        Object vec__9056 = object2;
        Object k = RT.nth((Object)vec__9056, (int)RT.uncheckedIntCast((long)0L), null);
        Object object3 = vec__9056;
        vec__9056 = null;
        Object v = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        Object object4 = temp__5455__auto__9066 = ((IFn)const__3.getRawRoot()).invoke((Object)new common$coerce_args$fn__9055$fn__9060(k), this_.predfns);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5455__auto__9066;
            temp__5455__auto__9066 = null;
            Object f = object5;
            Object object6 = m;
            m = null;
            Object object7 = k;
            k = null;
            Object object8 = f;
            f = null;
            Object object9 = v;
            v = null;
            this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object6, object7, ((IFn)object8).invoke(object9));
        } else {
            Object object10 = m;
            m = null;
            Object object11 = k;
            k = null;
            Object object12 = v;
            v = null;
            this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke(object10, object11, object12);
        }
        return object;
    }
}


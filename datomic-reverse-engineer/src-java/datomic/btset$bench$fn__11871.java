/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.iter.Iter;

public final class btset$bench$fn__11871
extends AFunction {
    Object bt;
    public static final Var const__0 = RT.var((String)"datomic.btset", (String)"seek");

    public btset$bench$fn__11871(Object object) {
        this.bt = object;
    }

    public Object invoke(Object p1__11862_SHARP_) {
        Object object;
        Object x;
        Object and__5236__auto__11873;
        Object object2 = and__5236__auto__11873 = (x = ((IFn)const__0.getRawRoot()).invoke(this_.bt, p1__11862_SHARP_));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = x;
            x = null;
            Object object4 = p1__11862_SHARP_;
            p1__11862_SHARP_ = null;
            btset$bench$fn__11871 this_ = null;
            object = Util.equiv((Object)((Iter)object3).get(), (Object)object4) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5236__auto__11873;
            Object var3_3 = null;
        }
        return object;
    }
}


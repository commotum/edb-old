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

public final class cli$unique_index$fn__20666
extends AFunction {
    Object k;
    Object v;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");

    public cli$unique_index$fn__20666(Object object, Object object2) {
        this.k = object;
        this.v = object2;
    }

    public Object invoke(Object m, Object x) {
        Object object;
        Object temp__5455__auto__20669;
        Object object2 = temp__5455__auto__20669 = RT.get((Object)x, (Object)this_.k);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object temp__5455__auto__20668;
            Object object3;
            Object object4 = temp__5455__auto__20669;
            temp__5455__auto__20669 = null;
            Object xk = object4;
            Object object5 = this_.v;
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = x;
                x = null;
                object3 = RT.get((Object)object6, (Object)this_.v);
            } else {
                object3 = x;
                x = null;
            }
            Object object7 = temp__5455__auto__20668 = object3;
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object8 = temp__5455__auto__20668;
                temp__5455__auto__20668 = null;
                Object xv = object8;
                Object object9 = m;
                m = null;
                Object object10 = xk;
                xk = null;
                Object object11 = xv;
                xv = null;
                cli$unique_index$fn__20666 this_ = null;
                object = ((IFn)const__1.getRawRoot()).invoke(object9, object10, object11);
            } else {
                object = m;
                m = null;
            }
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;

public final class common$coerce_args$fn__9055$fn__9060
extends AFunction {
    Object k;

    public common$coerce_args$fn__9055$fn__9060(Object object) {
        this.k = object;
    }

    public Object invoke(Object p__9059) {
        Object object;
        Object object2 = p__9059;
        p__9059 = null;
        Object vec__9061 = object2;
        Object p = RT.nth((Object)vec__9061, (int)RT.uncheckedIntCast((long)0L), null);
        Object object3 = vec__9061;
        vec__9061 = null;
        Object f = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        Object object4 = p;
        p = null;
        Object object5 = ((IFn)object4).invoke(this.k);
        if (object5 != null && object5 != Boolean.FALSE) {
            object = f;
            f = null;
        } else {
            object = null;
        }
        return object;
    }
}


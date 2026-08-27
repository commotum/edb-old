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

public final class common$map__GT_env$fn__9044
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__4 = RT.var((String)"datomic.common", (String)"clj-key->env-key");

    public Object invoke(Object m, Object p__9043) {
        Object object = p__9043;
        p__9043 = null;
        Object vec__9045 = object;
        Object k = RT.nth((Object)vec__9045, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__9045;
        vec__9045 = null;
        Object v = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = m;
        m = null;
        Object object4 = k;
        k = null;
        Object object5 = v;
        v = null;
        common$map__GT_env$fn__9044 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object3, ((IFn)const__4.getRawRoot()).invoke(object4), object5);
    }
}


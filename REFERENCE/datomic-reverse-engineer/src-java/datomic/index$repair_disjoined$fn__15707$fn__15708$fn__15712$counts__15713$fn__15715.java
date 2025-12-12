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

public final class index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713$fn__15715
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");

    public Object invoke(Object m, Object p__15714) {
        Object object = p__15714;
        p__15714 = null;
        Object vec__15716 = object;
        Object k = RT.nth((Object)vec__15716, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__15716;
        vec__15716 = null;
        Object v = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object object3 = m;
        m = null;
        Object object4 = k;
        k = null;
        Object object5 = v;
        v = null;
        index$repair_disjoined$fn__15707$fn__15708$fn__15712$counts__15713$fn__15715 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object3, object4, (Object)RT.count((Object)object5));
    }
}


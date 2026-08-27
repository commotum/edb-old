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

public final class query$apply_pf
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"mapv");

    public static Object invokeStatic(Object p__19535) {
        Object object;
        Object pf;
        Object object2 = p__19535;
        p__19535 = null;
        Object vec__19536 = object2;
        Object result2 = RT.nth((Object)vec__19536, (int)RT.intCast((long)0L), null);
        Object object3 = vec__19536;
        vec__19536 = null;
        Object object4 = pf = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = pf;
            pf = null;
            Object object6 = result2;
            result2 = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object5, object6);
        } else {
            object = result2;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$apply_pf.invokeStatic(object2);
    }
}


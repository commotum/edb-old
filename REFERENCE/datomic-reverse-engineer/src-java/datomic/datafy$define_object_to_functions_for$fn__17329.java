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

public final class datafy$define_object_to_functions_for$fn__17329
extends AFunction {
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"define-method-to-fn");

    public Object invoke(Object p__17328) {
        Object object = p__17328;
        p__17328 = null;
        Object vec__17330 = object;
        Object meth = RT.nth((Object)vec__17330, (int)RT.intCast((long)0L), null);
        Object fname = RT.nth((Object)vec__17330, (int)RT.intCast((long)1L), null);
        Object object2 = vec__17330;
        vec__17330 = null;
        Object docstring = RT.nth((Object)object2, (int)RT.intCast((long)2L), null);
        Object object3 = meth;
        meth = null;
        Object object4 = fname;
        fname = null;
        Object object5 = docstring;
        docstring = null;
        datafy$define_object_to_functions_for$fn__17329 this_ = null;
        return ((IFn)const__4.getRawRoot()).invoke(object3, object4, object5);
    }
}


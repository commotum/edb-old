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

public final class external_sort$chunk_seq$fn__14404
extends AFunction {
    Object sizer;
    Object max_size;
    public static final Var const__3 = RT.var((String)"datomic.external-sort", (String)"next-chunk");

    public external_sort$chunk_seq$fn__14404(Object object, Object object2) {
        this.sizer = object;
        this.max_size = object2;
    }

    public Object invoke(Object p__14403) {
        Object object;
        Object more;
        Object object2 = p__14403;
        p__14403 = null;
        Object vec__14405 = object2;
        RT.nth((Object)vec__14405, (int)RT.intCast((long)0L), null);
        Object object3 = vec__14405;
        vec__14405 = null;
        Object object4 = more = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = more;
            more = null;
            external_sort$chunk_seq$fn__14404 this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(this_.max_size, this_.sizer, object5);
        } else {
            object = null;
        }
        return object;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class backup$backup_key__GT_seg_id
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"subs");

    public static Object invokeStatic(Object s) {
        Object object;
        int idx = ((String)s).lastIndexOf("/");
        if (0L <= (long)idx) {
            Object object2 = s;
            Object object3 = s;
            s = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2, (Object)Numbers.num((long)Numbers.inc((long)idx)), (Object)RT.count((Object)object3));
        } else {
            object = s;
            Object object4 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$backup_key__GT_seg_id.invokeStatic(object2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.excise.ExcisePred;

public final class excise$remove_QMARK_
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object epred, Object datom) {
        Object object;
        Object object2 = epred;
        epred = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof ExcisePred) {
                Object object4 = datom;
                datom = null;
                object = ((ExcisePred)object3).ep_remove_QMARK_(object4);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object5 = datom;
        datom = null;
        object = const__0.getRawRoot().invoke(object3, object5);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return excise$remove_QMARK_.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.excise", (String)"ep-remove?");
    }
}


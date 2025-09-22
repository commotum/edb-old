/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;

public final class log$last_by_nth
extends AFunction {
    public static Object invokeStatic(Object coll) {
        Object object;
        int c = RT.count((Object)coll);
        if ((long)c == 0L) {
            object = null;
        } else {
            Object object2 = coll;
            coll = null;
            object = RT.nth((Object)object2, (int)RT.intCast((long)Numbers.dec((long)c)));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$last_by_nth.invokeStatic(object2);
    }
}


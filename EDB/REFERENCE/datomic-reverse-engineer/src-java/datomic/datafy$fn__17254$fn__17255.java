/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.data.EqualityPartition
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.data.EqualityPartition;
import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datafy$fn__17254$fn__17255
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object obj, Object type) {
        v0 = obj;
        obj = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == datafy$fn__17254$fn__17255.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof EqualityPartition)) {
            v1 = v1;
            datafy$fn__17254$fn__17255.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = datafy$fn__17254$fn__17255.const__0.getRawRoot().invoke(v1);
        } else {
            v2 = ((EqualityPartition)v1).equality_partition();
        }
        v3 = type;
        type = null;
        return Tuple.create((Object)v2, (Object)v3);
    }

    static {
        const__0 = RT.var((String)"clojure.data", (String)"equality-partition");
    }
}


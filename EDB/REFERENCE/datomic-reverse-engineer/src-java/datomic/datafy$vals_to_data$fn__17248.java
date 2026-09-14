/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datafy.ObjectToData;

public final class datafy$vals_to_data$fn__17248
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__3;
    public static final Var const__4;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object m, Object p__17247) {
        v0 = p__17247;
        p__17247 = null;
        vec__17249 = v0;
        k = RT.nth((Object)vec__17249, (int)RT.intCast((long)0L), null);
        v1 = vec__17249;
        vec__17249 = null;
        v = RT.nth((Object)v1, (int)RT.intCast((long)1L), null);
        v2 = (IFn)datafy$vals_to_data$fn__17248.const__3.getRawRoot();
        v3 = m;
        m = null;
        v4 = k;
        k = null;
        v5 = v;
        v = null;
        v6 = v5;
        if (Util.classOf((Object)v5) == datafy$vals_to_data$fn__17248.__cached_class__0) ** GOTO lbl20
        if (!(v6 instanceof ObjectToData)) {
            v6 = v6;
            datafy$vals_to_data$fn__17248.__cached_class__0 = Util.classOf((Object)v6);
lbl20:
            // 2 sources

            v7 = datafy$vals_to_data$fn__17248.const__4.getRawRoot().invoke(v6);
        } else {
            v7 = ((ObjectToData)v6).object_to_data();
        }
        this = null;
        return v2.invoke(v3, v4, v7);
    }

    static {
        const__3 = RT.var((String)"clojure.core", (String)"assoc!");
        const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data");
    }
}


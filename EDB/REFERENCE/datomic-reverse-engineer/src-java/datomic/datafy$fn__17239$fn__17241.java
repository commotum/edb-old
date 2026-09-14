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

public final class datafy$fn__17239$fn__17241
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__3;
    public static final Var const__4;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object m, Object p__17240) {
        v0 = p__17240;
        p__17240 = null;
        vec__17242 = v0;
        k = RT.nth((Object)vec__17242, (int)RT.intCast((long)0L), null);
        v1 = vec__17242;
        vec__17242 = null;
        v = RT.nth((Object)v1, (int)RT.intCast((long)1L), null);
        v2 = (IFn)datafy$fn__17239$fn__17241.const__3.getRawRoot();
        v3 = m;
        m = null;
        v4 = k;
        k = null;
        v5 = v4;
        if (Util.classOf((Object)v4) == datafy$fn__17239$fn__17241.__cached_class__0) ** GOTO lbl18
        if (!(v5 instanceof ObjectToData)) {
            v5 = v5;
            datafy$fn__17239$fn__17241.__cached_class__0 = Util.classOf((Object)v5);
lbl18:
            // 2 sources

            v6 = datafy$fn__17239$fn__17241.const__4.getRawRoot().invoke(v5);
        } else {
            v6 = ((ObjectToData)v5).object_to_data();
        }
        v7 = v;
        v = null;
        v8 = v7;
        if (Util.classOf((Object)v7) == datafy$fn__17239$fn__17241.__cached_class__1) ** GOTO lbl28
        if (!(v8 instanceof ObjectToData)) {
            v8 = v8;
            datafy$fn__17239$fn__17241.__cached_class__1 = Util.classOf((Object)v8);
lbl28:
            // 2 sources

            v9 = datafy$fn__17239$fn__17241.const__4.getRawRoot().invoke(v8);
        } else {
            v9 = ((ObjectToData)v8).object_to_data();
        }
        this = null;
        return v2.invoke(v3, v6, v9);
    }

    static {
        const__3 = RT.var((String)"clojure.core", (String)"assoc");
        const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data");
    }
}


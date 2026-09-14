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
import datomic.query.Immutify;

public final class query$fn__19236$fn__19238
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__3;
    public static final Var const__4;

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object m, Object p__19237) {
        v0 = p__19237;
        p__19237 = null;
        vec__19239 = v0;
        k = RT.nth((Object)vec__19239, (int)RT.intCast((long)0L), null);
        v1 = vec__19239;
        vec__19239 = null;
        v = RT.nth((Object)v1, (int)RT.intCast((long)1L), null);
        v2 = (IFn)query$fn__19236$fn__19238.const__3.getRawRoot();
        v3 = m;
        m = null;
        v4 = k;
        k = null;
        v5 = v4;
        if (Util.classOf((Object)v4) == query$fn__19236$fn__19238.__cached_class__0) ** GOTO lbl18
        if (!(v5 instanceof Immutify)) {
            v5 = v5;
            query$fn__19236$fn__19238.__cached_class__0 = Util.classOf((Object)v5);
lbl18:
            // 2 sources

            v6 = query$fn__19236$fn__19238.const__4.getRawRoot().invoke(v5);
        } else {
            v6 = ((Immutify)v5).immutify();
        }
        v7 = v;
        v = null;
        v8 = v7;
        if (Util.classOf((Object)v7) == query$fn__19236$fn__19238.__cached_class__1) ** GOTO lbl28
        if (!(v8 instanceof Immutify)) {
            v8 = v8;
            query$fn__19236$fn__19238.__cached_class__1 = Util.classOf((Object)v8);
lbl28:
            // 2 sources

            v9 = query$fn__19236$fn__19238.const__4.getRawRoot().invoke(v8);
        } else {
            v9 = ((Immutify)v8).immutify();
        }
        this = null;
        return v2.invoke(v3, v6, v9);
    }

    static {
        const__3 = RT.var((String)"clojure.core", (String)"assoc!");
        const__4 = RT.var((String)"datomic.query", (String)"immutify");
    }
}


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
import datomic.cluster.Dbid;

public final class log$tail_pod_key
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs) {
        v0 = cs;
        cs = null;
        v1 = v0;
        if (Util.classOf((Object)v0) == log$tail_pod_key.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof Dbid)) {
            v1 = v1;
            log$tail_pod_key.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = log$tail_pod_key.const__0.getRawRoot().invoke(v1);
        } else {
            v2 = ((Dbid)v1).dbId();
        }
        v3 = temp__5457__auto__16173 = v2;
        if (v3 != null && v3 != Boolean.FALSE) {
            v4 = temp__5457__auto__16173;
            temp__5457__auto__16173 = null;
            v5 = dbid = v4;
            dbid = null;
            v6 = ((IFn)log$tail_pod_key.const__1.getRawRoot()).invoke((Object)"pod-log-tail/", v5);
        } else {
            v6 = null;
        }
        return v6;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$tail_pod_key.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.cluster", (String)"dbId");
        const__1 = RT.var((String)"clojure.core", (String)"str");
    }
}


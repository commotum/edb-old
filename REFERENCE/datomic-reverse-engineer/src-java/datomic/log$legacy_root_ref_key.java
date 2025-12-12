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

public final class log$legacy_root_ref_key
extends AFunction {
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs) {
        block5: {
            block4: {
                v0 = cs;
                if (Util.classOf((Object)v0) == log$legacy_root_ref_key.__cached_class__0) ** GOTO lbl6
                if (!(v0 instanceof Dbid)) {
                    v0 = v0;
                    log$legacy_root_ref_key.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
                    // 2 sources

                    v1 = log$legacy_root_ref_key.const__0.getRawRoot().invoke(v0);
                } else {
                    v1 = ((Dbid)v0).dbId();
                }
                v2 = temp__5457__auto__16181 = v1;
                if (v2 == null || v2 == Boolean.FALSE) break block4;
                temp__5457__auto__16181 = null;
                v3 = (IFn)log$legacy_root_ref_key.const__1.getRawRoot();
                v4 = cs;
                cs = null;
                v5 = v4;
                if (Util.classOf((Object)v4) == log$legacy_root_ref_key.__cached_class__1) ** GOTO lbl21
                if (!(v5 instanceof Dbid)) {
                    v5 = v5;
                    log$legacy_root_ref_key.__cached_class__1 = Util.classOf((Object)v5);
lbl21:
                    // 2 sources

                    v6 = log$legacy_root_ref_key.const__0.getRawRoot().invoke(v5);
                } else {
                    v6 = ((Dbid)v5).dbId();
                }
                v7 = v3.invoke((Object)"ref-log-root/", v6);
                break block5;
            }
            v7 = null;
        }
        return v7;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$legacy_root_ref_key.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.cluster", (String)"dbId");
        const__1 = RT.var((String)"clojure.core", (String)"str");
    }
}


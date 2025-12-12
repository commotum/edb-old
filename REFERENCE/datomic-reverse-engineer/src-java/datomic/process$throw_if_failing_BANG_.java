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
import datomic.process.CriticalFailure;

public final class process$throw_if_failing_BANG_
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic() {
        v0 = process$throw_if_failing_BANG_.const__1.getRawRoot();
        if (Util.classOf((Object)v0) == process$throw_if_failing_BANG_.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof CriticalFailure)) {
            v0 = v0;
            process$throw_if_failing_BANG_.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = process$throw_if_failing_BANG_.const__0.getRawRoot().invoke(v0);
        } else {
            v1 = ((CriticalFailure)v0).failing_QMARK_();
        }
        if (v1 != null && v1 != Boolean.FALSE) {
            throw (Throwable)new RuntimeException("Process is shutting down");
        }
        return null;
    }

    public Object invoke() {
        return process$throw_if_failing_BANG_.invokeStatic();
    }

    static {
        const__0 = RT.var((String)"datomic.process", (String)"failing?");
        const__1 = RT.var((String)"datomic.process", (String)"instance");
    }
}


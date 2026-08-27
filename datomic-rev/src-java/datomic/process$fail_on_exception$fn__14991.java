/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.process.CriticalFailure;

public final class process$fail_on_exception$fn__14991
extends RestFn {
    Object f;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;

    public process$fail_on_exception$fn__14991(Object object) {
        this.f = object;
    }

    /*
     * Unable to fully structure code
     */
    public Object doInvoke(Object args) {
        try {
            v0 = args;
            args = null;
            var2_2 = ((IFn)process$fail_on_exception$fn__14991.const__0.getRawRoot()).invoke(this.f, v0);
        }
        catch (Throwable t) {
            v1 = process$fail_on_exception$fn__14991.const__2.getRawRoot();
            if (Util.classOf((Object)v1) == process$fail_on_exception$fn__14991.__cached_class__0) ** GOTO lbl12
            if (!(v1 instanceof CriticalFailure)) {
                v1 = v1;
                process$fail_on_exception$fn__14991.__cached_class__0 = Util.classOf((Object)v1);
lbl12:
                // 2 sources

                v2 = process$fail_on_exception$fn__14991.const__1.getRawRoot().invoke(v1, (Object)"Unhandled exception", (Object)t);
            } else {
                v2 = ((CriticalFailure)v1).fail("Unhandled exception", t);
            }
            t = null;
            throw t;
        }
        return var2_2;
    }

    public int getRequiredArity() {
        return 0;
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"apply");
        const__1 = RT.var((String)"datomic.process", (String)"fail");
        const__2 = RT.var((String)"datomic.process", (String)"instance");
    }
}


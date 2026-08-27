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
package datomic.memcached;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.memcached.RecoveringClientImpl;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class RecoveringClient$fn__10028
extends AFunction {
    Object this;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;

    public RecoveringClient$fn__10028(Object object) {
        this.this = object;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke(Object p1__10023_SHARP_) {
        try {
            v0 = p1__10023_SHARP_;
            p1__10023_SHARP_ = null;
            var2_2 = ((IFn)RecoveringClient$fn__10028.const__0.getRawRoot()).invoke(v0);
        }
        catch (Throwable t) {
            v1 = this.this;
            if (Util.classOf((Object)v1) == RecoveringClient$fn__10028.__cached_class__0) ** GOTO lbl12
            if (!(v1 instanceof RecoveringClientImpl)) {
                v1 = v1;
                RecoveringClient$fn__10028.__cached_class__0 = Util.classOf((Object)v1);
lbl12:
                // 2 sources

                v2 = RecoveringClient$fn__10028.const__1.getRawRoot().invoke(v1);
            } else {
                v2 = ((RecoveringClientImpl)v1).rc_reset_if_crashed();
            }
            var2_2 = v2;
        }
        return var2_2;
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.memcached", (String)"rc-reset-if-crashed");
    }
}


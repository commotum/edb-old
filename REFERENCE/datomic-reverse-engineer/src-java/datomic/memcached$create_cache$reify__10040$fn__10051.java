/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.memcached.RecoveringClientImpl;

public final class memcached$create_cache$reify__10040$fn__10051
extends AFunction {
    Object k;
    Object client;
    private static Class __cached_class__0;
    public static final Var const__0;

    public memcached$create_cache$reify__10040$fn__10051(Object object, Object object2) {
        this.k = object;
        this.client = object2;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            try {
                v0 = this.client;
                if (Util.classOf((Object)v0) == memcached$create_cache$reify__10040$fn__10051.__cached_class__0) ** GOTO lbl8
                if (!(v0 instanceof RecoveringClientImpl)) {
                    v0 = v0;
                    memcached$create_cache$reify__10040$fn__10051.__cached_class__0 = Util.classOf((Object)v0);
lbl8:
                    // 2 sources

                    this.k = null;
                    v1 = memcached$create_cache$reify__10040$fn__10051.const__0.getRawRoot().invoke(v0, this.k);
                } else {
                    this.k = null;
                    v1 = ((RecoveringClientImpl)v0).rc_get(this.k);
                }
                var1_1 = Tuple.create((Object)v1);
            }
            catch (Throwable t) {
                t = null;
                var1_1 = Tuple.create(null, (Object)t);
            }
            var3_6 = var1_1;
        }
        catch (Throwable e) {
            e = null;
            var3_6 = e;
        }
        return var3_6;
    }

    static {
        const__0 = RT.var((String)"datomic.memcached", (String)"rc-get");
    }
}


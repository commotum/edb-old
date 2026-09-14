/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class cluster$reset_ref$fn__10625
extends AFunction {
    Object rev;
    Object k;
    Object cluster;
    Object v;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Object const__3;
    public static final Keyword const__4;

    public cluster$reset_ref$fn__10625(Object object, Object object2, Object object3, Object object4) {
        this.rev = object;
        this.k = object2;
        this.cluster = object3;
        this.v = object4;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = cluster$reset_ref$fn__10625.const__0;
            v1 = this.cluster;
            this.cluster = null;
            v2 = v1;
            if (Util.classOf((Object)v1) == cluster$reset_ref$fn__10625.__cached_class__0) ** GOTO lbl11
            if (!(v2 instanceof ClusteredStore)) {
                v2 = v2;
                cluster$reset_ref$fn__10625.__cached_class__0 = Util.classOf((Object)v2);
lbl11:
                // 2 sources

                this.k = null;
                v3 = this.rev;
                if (v3 != null && v3 != Boolean.FALSE) {
                    this.rev = null;
                    v4 = Numbers.inc((Object)this.rev);
                } else {
                    v4 = cluster$reset_ref$fn__10625.const__3;
                }
                this.v = null;
                v5 = cluster$reset_ref$fn__10625.const__1.getRawRoot().invoke(v2, this.k, v4, this.v);
            } else {
                v6 = (ClusteredStore)v2;
                this.k = null;
                v7 = this.rev;
                if (v7 != null && v7 != Boolean.FALSE) {
                    this.rev = null;
                    v8 = Numbers.inc((Object)this.rev);
                } else {
                    v8 = cluster$reset_ref$fn__10625.const__3;
                }
                this.v = null;
                v5 = v6.set_ref(this.k, v8, this.v);
            }
            v0[1] = v5;
            var1_1 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v9 = new Object[2];
            v9[0] = cluster$reset_ref$fn__10625.const__4;
            t__8983__auto__ = null;
            v9[1] = t__8983__auto__;
            var1_1 = RT.mapUniqueKeys((Object[])v9);
        }
        return var1_1;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"datomic.cluster", (String)"set-ref");
        const__3 = 0L;
        const__4 = RT.keyword(null, (String)"threw");
    }
}


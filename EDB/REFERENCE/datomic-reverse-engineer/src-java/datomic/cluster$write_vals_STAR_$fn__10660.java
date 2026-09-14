/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class cluster$write_vals_STAR_$fn__10660
extends AFunction {
    Object cs;
    Object pacing;
    private static Class __cached_class__0;
    public static final Var const__3;

    public cluster$write_vals_STAR_$fn__10660(Object object, Object object2) {
        this.cs = object;
        this.pacing = object2;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object p__10659) {
        Object object;
        Object object2 = p__10659;
        p__10659 = null;
        Object vec__10661 = object2;
        Object k = RT.nth((Object)vec__10661, (int)RT.intCast((long)0L), null);
        Object object3 = vec__10661;
        vec__10661 = null;
        Object v = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object object4 = this.pacing;
        if (object4 != null && object4 != Boolean.FALSE) {
            Thread.sleep(RT.longCast((Object)((Number)this.pacing)));
        }
        Number number = Numbers.num((long)System.nanoTime());
        Object object5 = this.cs;
        if (Util.classOf((Object)object5) != __cached_class__0) {
            if (object5 instanceof ClusteredStore) {
                Object object6 = k;
                k = null;
                Object object7 = v;
                v = null;
                object = ((ClusteredStore)object5).create_val(object6, object7);
                return Tuple.create((Object)number, (Object)object);
            }
            object5 = object5;
            __cached_class__0 = Util.classOf((Object)object5);
        }
        Object object8 = k;
        k = null;
        Object object9 = v;
        v = null;
        object = const__3.getRawRoot().invoke(object5, object8, object9);
        return Tuple.create((Object)number, (Object)object);
    }

    static {
        const__3 = RT.var((String)"datomic.cluster", (String)"create-val");
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;

public final class datalog$hashyf$fn__18115
extends AFunction {
    Object bindings;

    public datalog$hashyf$fn__18115(Object object) {
        this.bindings = object;
    }

    public Object invoke(Object y) {
        long h = 0L;
        for (long i = 0L; i < (long)((Object[])this.bindings).length; ++i) {
            long l;
            Object b;
            Object object = b = RT.aget((Object[])((Object[])this.bindings), (int)((int)i));
            if (object != null && object != Boolean.FALSE) {
                Object object2 = b;
                b = null;
                l = Util.hashCombine((int)RT.uncheckedIntCast((long)h), (int)Util.hash((Object)RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)object2)))));
            } else {
                l = h;
            }
            h = l;
        }
        return Numbers.num((long)h);
    }
}


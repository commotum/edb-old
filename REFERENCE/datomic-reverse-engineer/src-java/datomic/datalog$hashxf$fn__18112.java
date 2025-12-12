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

public final class datalog$hashxf$fn__18112
extends AFunction {
    Object bindings;

    public datalog$hashxf$fn__18112(Object object) {
        this.bindings = object;
    }

    public Object invoke(Object x) {
        long h = 0L;
        for (long i = 0L; i < (long)((Object[])this.bindings).length; ++i) {
            Object b;
            Object object = b = RT.aget((Object[])((Object[])this.bindings), (int)((int)i));
            b = null;
            h = object != null && object != Boolean.FALSE ? (long)Util.hashCombine((int)RT.uncheckedIntCast((long)h), (int)Util.hash((Object)RT.nth((Object)x, (int)RT.uncheckedIntCast((long)i)))) : h;
        }
        return Numbers.num((long)h);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;

public final class datalog$matchf$fn__18109
extends AFunction {
    Object bindings;

    public datalog$matchf$fn__18109(Object object) {
        this.bindings = object;
    }

    public Object invoke(Object x, Object y) {
        Boolean bl;
        block3: {
            long i = 0L;
            while (i < (long)((Object[])this.bindings).length) {
                Object b;
                Object object = b = RT.aget((Object[])((Object[])this.bindings), (int)((int)i));
                if (object != null && object != Boolean.FALSE) {
                    Object object2 = b;
                    b = null;
                    if (Util.equiv((Object)RT.nth((Object)x, (int)RT.uncheckedIntCast((long)i)), (Object)RT.nth((Object)y, (int)RT.uncheckedIntCast((Object)((Number)object2))))) {
                        ++i;
                        continue;
                    }
                    bl = Boolean.FALSE;
                    break block3;
                }
                ++i;
            }
            bl = Boolean.TRUE;
        }
        return bl;
    }
}


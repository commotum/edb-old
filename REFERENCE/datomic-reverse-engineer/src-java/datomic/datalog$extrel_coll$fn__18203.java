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

public final class datalog$extrel_coll$fn__18203
extends AFunction {
    Object consts;

    public datalog$extrel_coll$fn__18203(Object object) {
        this.consts = object;
    }

    public Object invoke(Object p1__18202_SHARP_) {
        Boolean bl;
        block2: {
            for (long i = 0L; i < (long)RT.count((Object)this.consts); ++i) {
                boolean or__5238__auto__18205 = Util.identical((Object)RT.nth((Object)this.consts, (int)RT.uncheckedIntCast((long)i)), null);
                if (or__5238__auto__18205 ? or__5238__auto__18205 : Util.equiv((Object)RT.nth((Object)p1__18202_SHARP_, (int)RT.uncheckedIntCast((long)i)), (Object)RT.nth((Object)this.consts, (int)RT.uncheckedIntCast((long)i)))) {
                    continue;
                }
                bl = Boolean.FALSE;
                break block2;
            }
            bl = Boolean.TRUE;
        }
        return bl;
    }
}


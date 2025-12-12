/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$DLO
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$merge_db_STAR_$build_tiered_index__15765$ratio__15805
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.math", (String)"round");

    public Object invoke(Object num, Object denom) {
        Object object;
        if (Numbers.isZero((Object)denom)) {
            object = null;
        } else {
            Object object2 = num;
            num = null;
            Object object3 = denom;
            denom = null;
            object = ((IFn.DLO)const__1.getRawRoot()).invokePrim(Numbers.divide((double)RT.doubleCast((Object)object2), (Object)object3), 2L);
        }
        return object;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import datomic.index.TransposedData;

public final class index$fn__14999$__GT_TransposedData__15007
extends AFunction {
    public Object invoke(Object cnt, Object eas, Object vs, Object ts, Object ops) {
        Object object = cnt;
        cnt = null;
        Object object2 = eas;
        eas = null;
        Object object3 = vs;
        vs = null;
        Object object4 = ts;
        ts = null;
        Object object5 = ops;
        ops = null;
        return new TransposedData(RT.uncheckedIntCast((Object)((Number)object)), object2, object3, object4, object5);
    }
}


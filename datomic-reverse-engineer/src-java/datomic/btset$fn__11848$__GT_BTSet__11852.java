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
import datomic.btset.BTSet;

public final class btset$fn__11848$__GT_BTSet__11852
extends AFunction {
    public Object invoke(Object cmp, Object cnt, Object root) {
        Object object = cmp;
        cmp = null;
        Object object2 = cnt;
        cnt = null;
        Object object3 = root;
        root = null;
        return new BTSet(object, RT.uncheckedLongCast((Object)((Number)object2)), object3);
    }
}


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
import datomic.btset.BTSetIter;

public final class btset$fn__11821$__GT_BTSetIter__11827
extends AFunction {
    public Object invoke(Object branches, Object leaf, Object offset) {
        Object object = branches;
        branches = null;
        Object object2 = leaf;
        leaf = null;
        Object object3 = offset;
        offset = null;
        return new BTSetIter(object, object2, RT.uncheckedLongCast((Object)((Number)object3)));
    }
}


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
import datomic.btset.BTSetIterLink;

public final class btset$fn__11807$__GT_BTSetIterLink__11809
extends AFunction {
    public Object invoke(Object branch, Object offset, Object parent) {
        Object object = branch;
        branch = null;
        Object object2 = offset;
        offset = null;
        Object object3 = parent;
        parent = null;
        return new BTSetIterLink(object, RT.uncheckedLongCast((Object)((Number)object2)), object3);
    }
}


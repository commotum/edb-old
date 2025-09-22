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
import datomic.index.TreeIter;

public final class index$fn__15091$__GT_TreeIter__15176
extends AFunction {
    public Object invoke(Object lookup, Object root, Object ridx, Object dir, Object didx, Object seg, Object sidx) {
        Object object = lookup;
        lookup = null;
        Object object2 = root;
        root = null;
        Object object3 = ridx;
        ridx = null;
        Object object4 = dir;
        dir = null;
        Object object5 = didx;
        didx = null;
        Object object6 = seg;
        seg = null;
        Object object7 = sidx;
        sidx = null;
        return new TreeIter(object, object2, RT.uncheckedIntCast((Object)((Number)object3)), object4, RT.uncheckedIntCast((Object)((Number)object5)), object6, RT.uncheckedIntCast((Object)((Number)object7)));
    }
}


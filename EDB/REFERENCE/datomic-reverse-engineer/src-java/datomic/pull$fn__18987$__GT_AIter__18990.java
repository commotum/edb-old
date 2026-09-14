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
import datomic.pull.AIter;

public final class pull$fn__18987$__GT_AIter__18990
extends AFunction {
    public Object invoke(Object db2, Object e, Object a) {
        Object object = db2;
        db2 = null;
        Object object2 = e;
        e = null;
        Object object3 = a;
        a = null;
        return new AIter(object, RT.longCast((Object)((Number)object2)), RT.longCast((Object)((Number)object3)));
    }
}


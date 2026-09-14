/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic.query;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;

public final class EntityMap$fn__19194
extends AFunction {
    public Object invoke(Object p__19193) {
        Object v;
        Object object = p__19193;
        p__19193 = null;
        Object vec__19195 = object;
        RT.nth((Object)vec__19195, (int)RT.intCast((long)0L), null);
        Object object2 = vec__19195;
        vec__19195 = null;
        Object object3 = v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        v = null;
        EntityMap$fn__19194 this_ = null;
        return Util.identical((Object)object3, null) ? Boolean.TRUE : Boolean.FALSE;
    }
}


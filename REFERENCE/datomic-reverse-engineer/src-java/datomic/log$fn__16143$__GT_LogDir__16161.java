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
import datomic.log.LogDir;

public final class log$fn__16143$__GT_LogDir__16161
extends AFunction {
    public Object invoke(Object t, Object uuid) {
        Object object = t;
        t = null;
        Object object2 = uuid;
        uuid = null;
        return new LogDir(RT.longCast((Object)((Number)object)), object2);
    }
}


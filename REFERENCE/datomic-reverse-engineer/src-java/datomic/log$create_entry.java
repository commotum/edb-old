/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log.LogDir;

public final class log$create_entry
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.log", (String)"create-entry");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"rand-uuid");

    public static Object invokeStatic(Object t, Object uuid) {
        Object object = t;
        t = null;
        Object object2 = uuid;
        uuid = null;
        return new LogDir(RT.longCast((Object)((Number)object)), object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$create_entry.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object t) {
        Object object = t;
        t = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke());
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$create_entry.invokeStatic(object2);
    }
}


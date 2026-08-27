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

public final class db$on_schema_level
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"schema-level-fns");

    public static Object invokeStatic(Object db2, Object level) {
        Object object;
        Object temp__5455__auto__14137;
        Object object2 = level;
        level = null;
        Object object3 = temp__5455__auto__14137 = RT.get((Object)const__1.getRawRoot(), (Object)object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object f;
            Object object4 = temp__5455__auto__14137;
            temp__5455__auto__14137 = null;
            Object object5 = f = object4;
            f = null;
            Object object6 = db2;
            db2 = null;
            object = ((IFn)object5).invoke(object6);
        } else {
            object = db2;
            Object object7 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$on_schema_level.invokeStatic(object3, object4);
    }
}


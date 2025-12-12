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

public final class catalog$rename
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");

    public static Object invokeStatic(Object catalog2, Object db_name, Object new_name) {
        Object object = catalog2;
        Object object2 = new_name;
        new_name = null;
        Object object3 = catalog2;
        catalog2 = null;
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(object, object2, RT.get((Object)object3, (Object)db_name));
        Object object5 = db_name;
        db_name = null;
        return ((IFn)const__0.getRawRoot()).invoke(object4, object5);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return catalog$rename.invokeStatic(object4, object5, object6);
    }
}


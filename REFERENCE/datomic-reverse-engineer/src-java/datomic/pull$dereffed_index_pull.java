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

public final class pull$dereffed_index_pull
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"datomic.pull", (String)"index-pull");

    public static Object invokeStatic(Object db2, Object arg_map) {
        Object object = db2;
        db2 = null;
        Object object2 = arg_map;
        arg_map = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), ((IFn)const__3.getRawRoot()).invoke(object, object2)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return pull$dereffed_index_pull.invokeStatic(object3, object4);
    }
}


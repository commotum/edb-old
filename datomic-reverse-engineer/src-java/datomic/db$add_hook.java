/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$add_hook
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"hooks");
    public static final AFn const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 3});
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"growvec");

    public static Object invokeStatic(Object id, Object f) {
        Var var = const__0;
        var.setMeta((IPersistentMap)const__3);
        Object object = ((IFn)const__5.getRawRoot()).invoke(const__0.getRawRoot(), id);
        Object object2 = id;
        id = null;
        Object object3 = f;
        f = null;
        var.bindRoot(((IFn)const__4.getRawRoot()).invoke(object, object2, object3));
        return var;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$add_hook.invokeStatic(object3, object4);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.catalog$db_id__GT_db_name$fn__11088;

public final class catalog$db_id__GT_db_name
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object catalog2, Object db_id) {
        Object inverted;
        Object object = catalog2;
        catalog2 = null;
        Object object2 = inverted = ((IFn)const__0.getRawRoot()).invoke((Object)new catalog$db_id__GT_db_name$fn__11088(), (Object)PersistentArrayMap.EMPTY, object);
        inverted = null;
        Object object3 = db_id;
        db_id = null;
        return RT.get((Object)object2, (Object)object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$db_id__GT_db_name.invokeStatic(object3, object4);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.catalog$rename_database$condition__11124;
import datomic.catalog$rename_database$fn__11126;

public final class catalog$rename_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"update-catalog");
    public static final Var const__1 = RT.var((String)"datomic.catalog", (String)"update-succeeded?");
    public static final Keyword const__2 = RT.keyword(null, (String)"renamed-to");

    public static Object invokeStatic(Object cluster2, Object db_name, Object new_name) {
        Object object;
        catalog$rename_database$condition__11124 condition = new catalog$rename_database$condition__11124(db_name, new_name);
        Object object2 = cluster2;
        cluster2 = null;
        catalog$rename_database$condition__11124 catalog$rename_database$condition__11124 = condition;
        condition = null;
        Object object3 = db_name;
        db_name = null;
        Object resp = ((IFn)const__0.getRawRoot()).invoke(object2, (Object)catalog$rename_database$condition__11124, (Object)new catalog$rename_database$fn__11126(object3, new_name));
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(resp);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__2;
            Object object5 = new_name;
            new_name = null;
            objectArray[1] = object5;
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            object = resp;
            resp = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return catalog$rename_database.invokeStatic(object4, object5, object6);
    }
}


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
import datomic.catalog$remove_deleted_database$condition__11148;
import datomic.catalog$remove_deleted_database$fn__11150;

public final class catalog$remove_deleted_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"update-catalog");
    public static final Var const__1 = RT.var((String)"datomic.catalog", (String)"update-succeeded?");
    public static final Keyword const__2 = RT.keyword(null, (String)"removed");

    public static Object invokeStatic(Object cluster2, Object db_id) {
        Object object;
        catalog$remove_deleted_database$condition__11148 condition = new catalog$remove_deleted_database$condition__11148(db_id);
        Object object2 = cluster2;
        cluster2 = null;
        catalog$remove_deleted_database$condition__11148 catalog$remove_deleted_database$condition__11148 = condition;
        condition = null;
        Object resp = ((IFn)const__0.getRawRoot()).invoke(object2, (Object)catalog$remove_deleted_database$condition__11148, (Object)new catalog$remove_deleted_database$fn__11150(db_id));
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(resp);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__2;
            Object object4 = db_id;
            db_id = null;
            objectArray[1] = object4;
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            object = resp;
            Object var3_3 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$remove_deleted_database.invokeStatic(object3, object4);
    }
}


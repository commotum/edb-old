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
import datomic.catalog$undelete_database$condition__11140;
import datomic.catalog$undelete_database$fn__11142;

public final class catalog$undelete_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"update-catalog");
    public static final Var const__1 = RT.var((String)"datomic.catalog", (String)"update-succeeded?");
    public static final Keyword const__2 = RT.keyword(null, (String)"undeleted");
    public static final Keyword const__3 = RT.keyword(null, (String)"db-id");

    public static Object invokeStatic(Object cluster2, Object db_id, Object db_name) {
        Object object;
        catalog$undelete_database$condition__11140 condition = new catalog$undelete_database$condition__11140(db_name, db_id);
        Object object2 = cluster2;
        cluster2 = null;
        catalog$undelete_database$condition__11140 catalog$undelete_database$condition__11140 = condition;
        condition = null;
        Object resp = ((IFn)const__0.getRawRoot()).invoke(object2, (Object)catalog$undelete_database$condition__11140, (Object)new catalog$undelete_database$fn__11142(db_name, db_id));
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(resp);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__2;
            Object object4 = db_name;
            db_name = null;
            objectArray[1] = object4;
            objectArray[2] = const__3;
            Object object5 = db_id;
            db_id = null;
            objectArray[3] = object5;
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
        return catalog$undelete_database.invokeStatic(object4, object5, object6);
    }
}


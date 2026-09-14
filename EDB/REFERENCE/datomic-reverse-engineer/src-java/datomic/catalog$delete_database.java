/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.catalog$delete_database$condition__11133;
import datomic.catalog$delete_database$fn__11135;

public final class catalog$delete_database
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"update-catalog");
    public static final Var const__1 = RT.var((String)"datomic.catalog", (String)"update-succeeded?");
    public static final Keyword const__2 = RT.keyword(null, (String)"deleted");
    public static final Keyword const__3 = RT.keyword(null, (String)"db-id");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"clojure.set", (String)"difference");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"get-in");
    public static final AFn const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"new"), (Object)RT.keyword((String)"datomic", (String)"deleted"));
    public static final AFn const__11 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"old"), (Object)RT.keyword((String)"datomic", (String)"deleted"));

    public static Object invokeStatic(Object cluster2, Object db_name) {
        Object object;
        catalog$delete_database$condition__11133 condition = new catalog$delete_database$condition__11133(db_name);
        Object object2 = cluster2;
        cluster2 = null;
        catalog$delete_database$condition__11133 catalog$delete_database$condition__11133 = condition;
        condition = null;
        Object resp = ((IFn)const__0.getRawRoot()).invoke(object2, (Object)catalog$delete_database$condition__11133, (Object)new catalog$delete_database$fn__11135(db_name));
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(resp);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__2;
            Object object4 = db_name;
            db_name = null;
            objectArray[1] = object4;
            objectArray[2] = const__3;
            Object object5 = ((IFn)const__6.getRawRoot()).invoke(resp, (Object)const__9);
            Object object6 = resp;
            resp = null;
            objectArray[3] = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object5, ((IFn)const__6.getRawRoot()).invoke(object6, (Object)const__11)));
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
        return catalog$delete_database.invokeStatic(object3, object4);
    }
}


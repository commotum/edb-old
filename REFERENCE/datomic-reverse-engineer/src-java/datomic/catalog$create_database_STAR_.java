/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.catalog$create_database_STAR_$fn__11116;
import java.util.UUID;

public final class catalog$create_database_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"db-name");
    public static final Keyword const__4 = RT.keyword(null, (String)"db-id");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__7 = (AFn)Symbol.intern(null, (String)"db-name");
    public static final Var const__8 = RT.var((String)"datomic.catalog", (String)"valid-db-name?");
    public static final Var const__9 = RT.var((String)"datomic.catalog", (String)"update-catalog");
    public static final Var const__10 = RT.var((String)"datomic.catalog", (String)"conflict-check-fn");
    public static final Var const__11 = RT.var((String)"datomic.catalog", (String)"update-succeeded?");
    public static final Keyword const__12 = RT.keyword(null, (String)"created");
    public static final Keyword const__13 = RT.keyword(null, (String)"invalid-db-name");

    public static Object invokeStatic(Object cluster2, Object p__11114) {
        Object object;
        Object object2;
        Object object3 = p__11114;
        p__11114 = null;
        Object map__11115 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__11115);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__11115;
            map__11115 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__11115;
            map__11115 = null;
        }
        Object map__111152 = object2;
        Object db_name = RT.get((Object)map__111152, (Object)const__3);
        Object object6 = map__111152;
        map__111152 = null;
        Object db_id = RT.get((Object)object6, (Object)const__4);
        Object object7 = db_name;
        if (object7 == null || object7 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__5.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__6.getRawRoot()).invoke((Object)const__7))));
        }
        Object object8 = ((IFn)const__8.getRawRoot()).invoke(db_name);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9;
            Object or__5238__auto__11119;
            Object object10 = or__5238__auto__11119 = db_id;
            if (object10 != null && object10 != Boolean.FALSE) {
                object9 = or__5238__auto__11119;
                or__5238__auto__11119 = null;
            } else {
                object9 = ((IFn)const__5.getRawRoot()).invoke(db_name, (Object)"-", (Object)UUID.randomUUID());
            }
            Object assigned_db_id = object9;
            Object object11 = cluster2;
            cluster2 = null;
            Object object12 = db_id;
            db_id = null;
            Object resp = ((IFn)const__9.getRawRoot()).invoke(object11, ((IFn)const__10.getRawRoot()).invoke(db_name, object12), (Object)new catalog$create_database_STAR_$fn__11116(assigned_db_id, db_name));
            Object object13 = ((IFn)const__11.getRawRoot()).invoke(resp);
            if (object13 != null && object13 != Boolean.FALSE) {
                Object[] objectArray = new Object[4];
                objectArray[0] = const__12;
                Object object14 = db_name;
                db_name = null;
                objectArray[1] = object14;
                objectArray[2] = const__4;
                Object object15 = assigned_db_id;
                assigned_db_id = null;
                objectArray[3] = object15;
                object = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                object = resp;
                resp = null;
            }
        } else {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__13;
            Object object16 = db_name;
            db_name = null;
            objectArray[1] = object16;
            object = RT.mapUniqueKeys((Object[])objectArray);
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return catalog$create_database_STAR_.invokeStatic(object3, object4);
    }
}


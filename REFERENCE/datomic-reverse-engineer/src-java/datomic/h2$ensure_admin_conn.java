/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class h2$ensure_admin_conn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"data-dir");
    public static final Keyword const__4 = RT.keyword(null, (String)"storage-admin-password");
    public static final Keyword const__5 = RT.keyword(null, (String)"old-storage-admin-password");
    public static final Var const__6 = RT.var((String)"datomic.h2", (String)"try-connect");
    public static final Keyword const__7 = RT.keyword(null, (String)"username");
    public static final Keyword const__8 = RT.keyword(null, (String)"password");
    public static final Var const__9 = RT.var((String)"datomic.h2", (String)"updating-connect");
    public static final Keyword const__10 = RT.keyword(null, (String)"old-user");
    public static final Keyword const__11 = RT.keyword(null, (String)"old-password");
    public static final Keyword const__12 = RT.keyword(null, (String)"user");

    public static Object invokeStatic(Object p__11623) {
        Object object;
        Object or__5238__auto__11627;
        Object object2;
        Object object3 = p__11623;
        p__11623 = null;
        Object map__11624 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__11624);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__11624;
            map__11624 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__11624;
            map__11624 = null;
        }
        Object map__116242 = object2;
        Object data_dir = RT.get((Object)map__116242, (Object)const__3);
        Object storage_admin_password = RT.get((Object)map__116242, (Object)const__4);
        Object object6 = map__116242;
        map__116242 = null;
        Object old_storage_admin_password = RT.get((Object)object6, (Object)const__5);
        Object object7 = storage_admin_password;
        Object object8 = or__5238__auto__11627 = object7 != null && object7 != Boolean.FALSE ? ((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__3, data_dir, const__7, "", const__8, storage_admin_password})) : ((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__3, data_dir, const__7, "", const__8, ""}));
        if (object8 != null && object8 != Boolean.FALSE) {
            object = or__5238__auto__11627;
            or__5238__auto__11627 = null;
        } else {
            Object or__5238__auto__11626;
            Object[] objectArray = new Object[8];
            objectArray[0] = const__10;
            objectArray[1] = "";
            objectArray[2] = const__11;
            Object object9 = old_storage_admin_password;
            old_storage_admin_password = null;
            objectArray[3] = object9;
            objectArray[4] = const__12;
            objectArray[5] = "";
            objectArray[6] = const__8;
            objectArray[7] = storage_admin_password;
            Object object10 = or__5238__auto__11626 = ((IFn)const__9.getRawRoot()).invoke(data_dir, (Object)RT.mapUniqueKeys((Object[])objectArray));
            if (object10 != null && object10 != Boolean.FALSE) {
                object = or__5238__auto__11626;
                or__5238__auto__11626 = null;
            } else {
                Object object11 = data_dir;
                data_dir = null;
                Object[] objectArray2 = new Object[8];
                objectArray2[0] = const__10;
                objectArray2[1] = "";
                objectArray2[2] = const__11;
                objectArray2[3] = "";
                objectArray2[4] = const__12;
                objectArray2[5] = "";
                objectArray2[6] = const__8;
                Object object12 = storage_admin_password;
                storage_admin_password = null;
                objectArray2[7] = object12;
                object = ((IFn)const__9.getRawRoot()).invoke(object11, (Object)RT.mapUniqueKeys((Object[])objectArray2));
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return h2$ensure_admin_conn.invokeStatic(object2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb$ddb_item__GT_log_item
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.core2.aws.ddb", (String)"de-item-map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__7 = RT.keyword(null, (String)"r");
    public static final Keyword const__8 = RT.keyword(null, (String)"header");
    public static final Keyword const__9 = RT.keyword(null, (String)"body");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__11 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Keyword const__12 = RT.keyword(null, (String)"t");

    public static Object invokeStatic(Object ddb_item) {
        Object object;
        Object object2;
        Object object3 = ddb_item;
        ddb_item = null;
        Object map__20639 = ((IFn)const__0.getRawRoot()).invoke(object3);
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__20639);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__2.getRawRoot()).invoke(map__20639);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__20639;
                map__20639 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__3.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__4.getRawRoot()).invoke(map__20639);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__20639;
                    map__20639 = null;
                    object2 = ((IFn)const__5.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__20639;
            map__20639 = null;
        }
        Object map__206392 = object2;
        Object r = RT.get((Object)map__206392, (Object)const__7);
        Object header = RT.get((Object)map__206392, (Object)const__8);
        Object object9 = map__206392;
        map__206392 = null;
        Object body = RT.get((Object)object9, (Object)const__9);
        Object[] objectArray = new Object[2];
        objectArray[0] = const__8;
        Object object10 = header;
        header = null;
        Object object11 = r;
        r = null;
        objectArray[1] = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(object10), (Object)const__12, object11);
        IPersistentMap G__20640 = RT.mapUniqueKeys((Object[])objectArray);
        Object object12 = body;
        if (object12 != null && object12 != Boolean.FALSE) {
            IPersistentMap iPersistentMap = G__20640;
            G__20640 = null;
            Object object13 = body;
            body = null;
            object = ((IFn)const__10.getRawRoot()).invoke((Object)iPersistentMap, (Object)const__9, object13);
        } else {
            object = G__20640;
            G__20640 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$ddb_item__GT_log_item.invokeStatic(object2);
    }
}


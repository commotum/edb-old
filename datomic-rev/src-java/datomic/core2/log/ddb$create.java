/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.log;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb$create
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"client");
    public static final Keyword const__7 = RT.keyword(null, (String)"table");
    public static final Keyword const__8 = RT.keyword(null, (String)"p");
    public static final Keyword const__9 = RT.keyword(null, (String)"chunk-size");
    public static final Var const__10 = RT.var((String)"datomic.core2.log.ddb", (String)"->Log");

    public static Object invokeStatic(Object p__20861) {
        Object object;
        Object object2 = p__20861;
        p__20861 = null;
        Object map__20862 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__20862);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__20862);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = map__20862;
                map__20862 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object5)));
            } else {
                Object object6 = ((IFn)const__3.getRawRoot()).invoke(map__20862);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = map__20862;
                    map__20862 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object7);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__20862;
            map__20862 = null;
        }
        Object map__208622 = object;
        Object client2 = RT.get((Object)map__208622, (Object)const__6);
        Object table = RT.get((Object)map__208622, (Object)const__7);
        Object p = RT.get((Object)map__208622, (Object)const__8);
        Object object8 = map__208622;
        map__208622 = null;
        Object chunk_size = RT.get((Object)object8, (Object)const__9);
        Object object9 = client2;
        client2 = null;
        Object object10 = table;
        table = null;
        Object object11 = p;
        p = null;
        Object object12 = chunk_size;
        chunk_size = null;
        return ((IFn)const__10.getRawRoot()).invoke(object9, object10, object11, object12);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$create.invokeStatic(object2);
    }
}


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
package datomic.core2.atom;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class logged$next_header
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"tombstone");
    public static final Keyword const__7 = RT.keyword((String)"cognitect.anomalies", (String)"category");
    public static final Keyword const__8 = RT.keyword((String)"cognitect.anomalies", (String)"unavailable");
    public static final Keyword const__9 = RT.keyword((String)"cognitect.anomalies", (String)"message");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"update");
    public static final Keyword const__11 = RT.keyword(null, (String)"t");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"inc");
    public static final Keyword const__13 = RT.keyword(null, (String)"next-t");

    public static Object invokeStatic(Object p__19713) {
        Object object;
        Object tombstone;
        Object map__19714;
        Object object2;
        Object object3 = p__19713;
        p__19713 = null;
        Object map__197142 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__197142);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__197142);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__197142;
                map__197142 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__197142);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__197142;
                    map__197142 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__197142;
            map__197142 = null;
        }
        Object header = map__19714 = object2;
        Object object9 = map__19714;
        map__19714 = null;
        Object object10 = tombstone = RT.get((Object)object9, (Object)const__6);
        if (object10 != null && object10 != Boolean.FALSE) {
            Object[] objectArray = new Object[4];
            objectArray[0] = const__7;
            objectArray[1] = const__8;
            objectArray[2] = const__9;
            Object object11 = tombstone;
            tombstone = null;
            objectArray[3] = object11;
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            Object object12 = header;
            header = null;
            object = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke(object12, (Object)const__11, const__12.getRawRoot()), (Object)const__13, const__12.getRawRoot());
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return logged$next_header.invokeStatic(object2);
    }
}


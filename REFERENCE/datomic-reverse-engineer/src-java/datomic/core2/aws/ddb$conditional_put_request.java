/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.aws;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class ddb$conditional_put_request
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"table");
    public static final Keyword const__7 = RT.keyword(null, (String)"p");
    public static final Keyword const__8 = RT.keyword(null, (String)"r");
    public static final Keyword const__9 = RT.keyword(null, (String)"item");
    public static final Keyword const__10 = RT.keyword(null, (String)"op");
    public static final Keyword const__11 = RT.keyword(null, (String)"PutItem");
    public static final Keyword const__12 = RT.keyword(null, (String)"request");
    public static final Keyword const__13 = RT.keyword(null, (String)"TableName");
    public static final Keyword const__14 = RT.keyword(null, (String)"Item");
    public static final Var const__15 = RT.var((String)"datomic.core2.aws.ddb", (String)"item-map");
    public static final Keyword const__16 = RT.keyword(null, (String)"Expected");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"name");
    public static final AFn const__19 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"Exists"), Boolean.FALSE});
    public static final AFn const__20 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"Exists"), Boolean.FALSE});

    public static Object invokeStatic(Object p__20440) {
        Object object;
        Object object2 = p__20440;
        p__20440 = null;
        Object map__20441 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__20441);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__20441);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = map__20441;
                map__20441 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object5)));
            } else {
                Object object6 = ((IFn)const__3.getRawRoot()).invoke(map__20441);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = map__20441;
                    map__20441 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object7);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__20441;
            map__20441 = null;
        }
        Object map__204412 = object;
        Object table = RT.get((Object)map__204412, (Object)const__6);
        Object p = RT.get((Object)map__204412, (Object)const__7);
        Object r = RT.get((Object)map__204412, (Object)const__8);
        Object object8 = map__204412;
        map__204412 = null;
        Object item = RT.get((Object)object8, (Object)const__9);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__10;
        objectArray[1] = const__11;
        objectArray[2] = const__12;
        Object[] objectArray2 = new Object[6];
        objectArray2[0] = const__13;
        Object object9 = table;
        table = null;
        objectArray2[1] = object9;
        objectArray2[2] = const__14;
        Object object10 = item;
        item = null;
        objectArray2[3] = ((IFn)const__15.getRawRoot()).invoke(object10);
        objectArray2[4] = const__16;
        Object[] objectArray3 = new Object[4];
        Object object11 = p;
        p = null;
        objectArray3[0] = ((IFn)const__17.getRawRoot()).invoke(object11);
        objectArray3[1] = const__19;
        Object object12 = r;
        r = null;
        objectArray3[2] = ((IFn)const__17.getRawRoot()).invoke(object12);
        objectArray3[3] = const__20;
        objectArray2[5] = RT.map((Object[])objectArray3);
        objectArray[3] = RT.mapUniqueKeys((Object[])objectArray2);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$conditional_put_request.invokeStatic(object2);
    }
}


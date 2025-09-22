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
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class aws_api$get_object_request
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__7 = RT.keyword(null, (String)"key");
    public static final Keyword const__8 = RT.keyword(null, (String)"op");
    public static final Keyword const__9 = RT.keyword(null, (String)"GetObject");
    public static final Keyword const__10 = RT.keyword(null, (String)"request");
    public static final Keyword const__11 = RT.keyword(null, (String)"Bucket");
    public static final Keyword const__12 = RT.keyword(null, (String)"Key");

    public static Object invokeStatic(Object p__20453) {
        Object object;
        Object object2 = p__20453;
        p__20453 = null;
        Object map__20454 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__20454);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__20454);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = map__20454;
                map__20454 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object5)));
            } else {
                Object object6 = ((IFn)const__3.getRawRoot()).invoke(map__20454);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = map__20454;
                    map__20454 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object7);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__20454;
            map__20454 = null;
        }
        Object map__204542 = object;
        Object bucket = RT.get((Object)map__204542, (Object)const__6);
        Object object8 = map__204542;
        map__204542 = null;
        Object key = RT.get((Object)object8, (Object)const__7);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__8;
        objectArray[1] = const__9;
        objectArray[2] = const__10;
        Object[] objectArray2 = new Object[4];
        objectArray2[0] = const__11;
        Object object9 = bucket;
        bucket = null;
        objectArray2[1] = object9;
        objectArray2[2] = const__12;
        Object object10 = key;
        key = null;
        objectArray2[3] = object10;
        objectArray[3] = RT.mapUniqueKeys((Object[])objectArray2);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws_api$get_object_request.invokeStatic(object2);
    }
}


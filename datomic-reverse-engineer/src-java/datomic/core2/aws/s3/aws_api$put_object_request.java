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
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class aws_api$put_object_request
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"body");
    public static final Keyword const__7 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__8 = RT.keyword(null, (String)"content-length");
    public static final Keyword const__9 = RT.keyword(null, (String)"key");
    public static final Keyword const__10 = RT.keyword(null, (String)"op");
    public static final Keyword const__11 = RT.keyword(null, (String)"PutObject");
    public static final Keyword const__12 = RT.keyword(null, (String)"request");
    public static final Keyword const__13 = RT.keyword(null, (String)"Bucket");
    public static final Keyword const__14 = RT.keyword(null, (String)"Key");
    public static final Keyword const__15 = RT.keyword(null, (String)"Body");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__17 = RT.keyword(null, (String)"ContentLength");

    public static Object invokeStatic(Object p__20456) {
        Object object;
        Object object2;
        Object object3 = p__20456;
        p__20456 = null;
        Object map__20457 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__20457);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__20457);
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = map__20457;
                map__20457 = null;
                object2 = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object6)));
            } else {
                Object object7 = ((IFn)const__3.getRawRoot()).invoke(map__20457);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = map__20457;
                    map__20457 = null;
                    object2 = ((IFn)const__4.getRawRoot()).invoke(object8);
                } else {
                    object2 = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object2 = map__20457;
            map__20457 = null;
        }
        Object map__204572 = object2;
        Object body = RT.get((Object)map__204572, (Object)const__6);
        Object bucket = RT.get((Object)map__204572, (Object)const__7);
        Object content_length = RT.get((Object)map__204572, (Object)const__8);
        Object object9 = map__204572;
        map__204572 = null;
        Object key = RT.get((Object)object9, (Object)const__9);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__10;
        objectArray[1] = const__11;
        objectArray[2] = const__12;
        Object[] objectArray2 = new Object[6];
        objectArray2[0] = const__13;
        Object object10 = bucket;
        bucket = null;
        objectArray2[1] = object10;
        objectArray2[2] = const__14;
        Object object11 = key;
        key = null;
        objectArray2[3] = object11;
        objectArray2[4] = const__15;
        Object object12 = body;
        body = null;
        objectArray2[5] = object12;
        IPersistentMap G__20458 = RT.mapUniqueKeys((Object[])objectArray2);
        Object object13 = content_length;
        if (object13 != null && object13 != Boolean.FALSE) {
            IPersistentMap iPersistentMap = G__20458;
            G__20458 = null;
            Object object14 = content_length;
            content_length = null;
            object = ((IFn)const__16.getRawRoot()).invoke((Object)iPersistentMap, (Object)const__17, object14);
        } else {
            object = G__20458;
            G__20458 = null;
        }
        objectArray[3] = object;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws_api$put_object_request.invokeStatic(object2);
    }
}


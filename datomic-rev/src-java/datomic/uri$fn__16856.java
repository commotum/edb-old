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
import java.util.Map;

public final class uri$fn__16856
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.uri", (String)"fixup-uri-map");
    public static final Var const__3 = RT.var((String)"datomic.uri", (String)"mapify-ddb+s3-uri");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__7 = RT.keyword(null, (String)"aws-dynamodb-table");
    public static final Keyword const__8 = RT.keyword(null, (String)"system");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__10 = RT.keyword(null, (String)"system-root");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object uri2) {
        Object object;
        Object or__5238__auto__16859;
        Object system;
        Object map__16857;
        Object object2;
        Object object3;
        if (uri2 instanceof Map) {
            Object object4 = uri2;
            uri2 = null;
            object3 = ((IFn)const__2.getRawRoot()).invoke(object4);
        } else {
            Object object5 = uri2;
            uri2 = null;
            object3 = ((IFn)const__3.getRawRoot()).invoke(object5);
        }
        Object map__168572 = object3;
        Object object6 = ((IFn)const__4.getRawRoot()).invoke(map__168572);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = map__168572;
            map__168572 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__5.getRawRoot()).invoke(object7)));
        } else {
            object2 = map__168572;
            map__168572 = null;
        }
        Object m = map__16857 = object2;
        Object aws_dynamodb_table = RT.get((Object)map__16857, (Object)const__7);
        Object object8 = map__16857;
        map__16857 = null;
        Object object9 = system = RT.get((Object)object8, (Object)const__8);
        system = null;
        Object object10 = or__5238__auto__16859 = object9;
        if (object10 != null && object10 != Boolean.FALSE) {
            object = or__5238__auto__16859;
            or__5238__auto__16859 = null;
        } else {
            object = "_default";
        }
        Object system2 = object;
        Object object11 = m;
        m = null;
        Object[] objectArray = new Object[4];
        objectArray[0] = const__8;
        objectArray[1] = system2;
        objectArray[2] = const__10;
        Object object12 = aws_dynamodb_table;
        aws_dynamodb_table = null;
        Object object13 = system2;
        system2 = null;
        objectArray[3] = ((IFn)const__11.getRawRoot()).invoke(object12, (Object)"/", object13);
        return ((IFn)const__9.getRawRoot()).invoke(object11, (Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$fn__16856.invokeStatic(object2);
    }
}


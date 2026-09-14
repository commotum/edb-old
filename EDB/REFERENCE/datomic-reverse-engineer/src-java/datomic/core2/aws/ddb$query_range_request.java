/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.core2.aws;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class ddb$query_range_request
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"table");
    public static final Keyword const__7 = RT.keyword(null, (String)"p");
    public static final Keyword const__8 = RT.keyword(null, (String)"r");
    public static final Keyword const__9 = RT.keyword(null, (String)"attrs");
    public static final Keyword const__10 = RT.keyword(null, (String)"forward");
    public static final Keyword const__11 = RT.keyword(null, (String)"limit");
    public static final Keyword const__12 = RT.keyword(null, (String)"op");
    public static final Keyword const__13 = RT.keyword(null, (String)"Query");
    public static final Keyword const__14 = RT.keyword(null, (String)"request");
    public static final Keyword const__15 = RT.keyword(null, (String)"TableName");
    public static final Keyword const__16 = RT.keyword(null, (String)"ConsistentRead");
    public static final Keyword const__17 = RT.keyword(null, (String)"ScanIndexForward");
    public static final Keyword const__18 = RT.keyword(null, (String)"KeyConditions");
    public static final Keyword const__19 = RT.keyword(null, (String)"ComparisonOperator");
    public static final Keyword const__20 = RT.keyword(null, (String)"AttributeValueList");
    public static final Var const__21 = RT.var((String)"datomic.core2.aws.ddb", (String)"attribute-value");
    public static final Keyword const__22 = RT.keyword(null, (String)"Limit");

    public static Object invokeStatic(Object p__20443) {
        Object object;
        Object object2 = p__20443;
        p__20443 = null;
        Object map__20444 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__20444);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__20444);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = map__20444;
                map__20444 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object5)));
            } else {
                Object object6 = ((IFn)const__3.getRawRoot()).invoke(map__20444);
                if (object6 != null && object6 != Boolean.FALSE) {
                    Object object7 = map__20444;
                    map__20444 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object7);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__20444;
            map__20444 = null;
        }
        Object map__204442 = object;
        Object table = RT.get((Object)map__204442, (Object)const__6);
        Object p = RT.get((Object)map__204442, (Object)const__7);
        Object r = RT.get((Object)map__204442, (Object)const__8);
        Object attrs = RT.get((Object)map__204442, (Object)const__9);
        Object forward = RT.get((Object)map__204442, (Object)const__10);
        Object object8 = map__204442;
        map__204442 = null;
        Object limit2 = RT.get((Object)object8, (Object)const__11);
        Object[] objectArray = new Object[4];
        objectArray[0] = const__12;
        objectArray[1] = const__13;
        objectArray[2] = const__14;
        Object[] objectArray2 = new Object[10];
        objectArray2[0] = const__15;
        Object object9 = table;
        table = null;
        objectArray2[1] = object9;
        objectArray2[2] = const__16;
        objectArray2[3] = Boolean.TRUE;
        objectArray2[4] = const__17;
        objectArray2[5] = forward;
        objectArray2[6] = const__18;
        Object[] objectArray3 = new Object[4];
        objectArray3[0] = p;
        Object[] objectArray4 = new Object[4];
        objectArray4[0] = const__19;
        objectArray4[1] = "EQ";
        objectArray4[2] = const__20;
        Object object10 = p;
        p = null;
        objectArray4[3] = Tuple.create((Object)((IFn)const__21.getRawRoot()).invoke(((IFn)object10).invoke(attrs)));
        objectArray3[1] = RT.mapUniqueKeys((Object[])objectArray4);
        objectArray3[2] = r;
        Object[] objectArray5 = new Object[4];
        objectArray5[0] = const__19;
        Object object11 = forward;
        forward = null;
        objectArray5[1] = object11 != null && object11 != Boolean.FALSE ? "GE" : "LE";
        objectArray5[2] = const__20;
        Object object12 = r;
        r = null;
        Object object13 = attrs;
        attrs = null;
        objectArray5[3] = Tuple.create((Object)((IFn)const__21.getRawRoot()).invoke(((IFn)object12).invoke(object13)));
        objectArray3[3] = RT.mapUniqueKeys((Object[])objectArray5);
        objectArray2[7] = RT.map((Object[])objectArray3);
        objectArray2[8] = const__22;
        Object object14 = limit2;
        limit2 = null;
        objectArray2[9] = object14;
        objectArray[3] = RT.mapUniqueKeys((Object[])objectArray2);
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$query_range_request.invokeStatic(object2);
    }
}


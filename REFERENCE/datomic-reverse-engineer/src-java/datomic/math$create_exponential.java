/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.math$create_exponential$fn__491;

public final class math$create_exponential
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"x1");
    public static final Keyword const__4 = RT.keyword(null, (String)"x2");
    public static final Keyword const__5 = RT.keyword(null, (String)"y1");
    public static final Keyword const__6 = RT.keyword(null, (String)"y2");

    public static Object invokeStatic(Object p__489) {
        Object y2;
        Object object;
        Object object2 = p__489;
        p__489 = null;
        Object map__490 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__490);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__490;
            map__490 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__490;
            map__490 = null;
        }
        Object map__4902 = object;
        Object x1 = RT.get((Object)map__4902, (Object)const__3);
        Object x2 = RT.get((Object)map__4902, (Object)const__4);
        Object y1 = RT.get((Object)map__4902, (Object)const__5);
        Object object5 = map__4902;
        map__4902 = null;
        Object object6 = y2 = RT.get((Object)object5, (Object)const__6);
        y2 = null;
        Object object7 = x2;
        x2 = null;
        double b = Math.pow(Numbers.divide((double)RT.floatCast((Object)object6), (Object)y1), Numbers.divide((double)1.0, (Object)Numbers.minus((Object)object7, (Object)x1)));
        Object object8 = y1;
        y1 = null;
        Object object9 = x1;
        x1 = null;
        double a = Numbers.divide((Object)object8, (double)Math.pow(b, RT.doubleCast((Object)((Number)object9))));
        return new math$create_exponential$fn__491(a, b);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return math$create_exponential.invokeStatic(object2);
    }
}


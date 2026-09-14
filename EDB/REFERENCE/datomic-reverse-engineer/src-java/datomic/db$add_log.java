/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class db$add_log
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"update-in");
    public static final AFn const__4 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"txes"));
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__6 = RT.keyword(null, (String)"t");
    public static final Keyword const__7 = RT.keyword(null, (String)"data");

    public static Object invokeStatic(Object memlog2, Object t, Object data2) {
        Object object;
        if (Numbers.lte((long)1000L, (Object)t)) {
            Object object2 = memlog2;
            memlog2 = null;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__6;
            Object object3 = t;
            t = null;
            objectArray[1] = object3;
            objectArray[2] = const__7;
            Object object4 = data2;
            data2 = null;
            objectArray[3] = object4;
            object = ((IFn)const__2.getRawRoot()).invoke(object2, (Object)const__4, const__5.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            object = memlog2;
            Object object5 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$add_log.invokeStatic(object4, object5, object6);
    }
}


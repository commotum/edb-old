/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class common$result_count
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not");

    public static Object invokeStatic(Object offset, Object limit2, Object coll) {
        Object object;
        Object object2;
        Object and__5236__auto__9266;
        Number number;
        Object object3 = coll;
        coll = null;
        int G__9264 = RT.count((Object)object3);
        Object object4 = offset;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = offset;
            offset = null;
            number = Numbers.unchecked_minus((long)G__9264, (Object)object5);
        } else {
            number = G__9264;
        }
        Integer G__92642 = number;
        Object object6 = and__5236__auto__9266 = limit2;
        if (object6 != null && object6 != Boolean.FALSE) {
            object2 = ((IFn)const__2.getRawRoot()).invoke((Object)(Numbers.isNeg((Object)limit2) ? Boolean.TRUE : Boolean.FALSE));
        } else {
            object2 = and__5236__auto__9266;
            and__5236__auto__9266 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Integer n = G__92642;
            G__92642 = null;
            Object object7 = limit2;
            limit2 = null;
            object = Numbers.min((Object)n, (Object)object7);
        } else {
            object = G__92642;
            G__92642 = null;
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
        return common$result_count.invokeStatic(object4, object5, object6);
    }
}


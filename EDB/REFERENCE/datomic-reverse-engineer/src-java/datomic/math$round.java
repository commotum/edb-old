/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$DLO
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

public final class math$round
extends AFunction
implements IFn.DLO {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Object const__6 = 10L;

    public static Object invokeStatic(double num, long l) {
        Double d;
        if (num == 0.0) {
            d = num;
        } else {
            double d2 = Math.ceil(Math.log10(Numbers.lt((double)num, (long)0L) ? -num : num));
            double power = Numbers.minus((long)l, (double)d2);
            double mag = Math.pow(RT.doubleCast((Object)((Number)const__6)), power);
            long shifted = Math.round(num * mag);
            d = Numbers.divide((long)shifted, (double)mag);
        }
        return d;
    }

    public Object invoke(Object object, Object object2) {
        return math$round.invokeStatic(RT.doubleCast((Object)((Number)object)), RT.longCast((Object)((Number)object2)));
    }

    public final Object invokePrim(double d, long l) {
        return math$round.invokeStatic(d, l);
    }

    public static Object invokeStatic(Object num) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(num);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = num;
            num = null;
        } else {
            Object object3 = num;
            num = null;
            object = Numbers.num((long)Math.round(RT.doubleCast((Object)object3)));
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return math$round.invokeStatic(object2);
    }
}


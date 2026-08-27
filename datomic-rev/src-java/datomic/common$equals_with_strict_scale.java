/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.math.BigDecimal;

public final class common$equals_with_strict_scale
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"compare");

    /*
     * WARNING - void declaration
     */
    public static Object invokeStatic(Object a, Object b) {
        Boolean bl;
        boolean and__5236__auto__9023 = Numbers.isZero((long)((IFn.OOL)const__1.getRawRoot()).invokePrim(a, b));
        if (and__5236__auto__9023) {
            void var3_3;
            boolean and__5236__auto__9022 = a instanceof BigDecimal;
            if (and__5236__auto__9022 ? b instanceof BigDecimal : var3_3) {
                Object object = a;
                a = null;
                Object object2 = b;
                b = null;
                bl = Util.equiv((long)((BigDecimal)object).scale(), (long)((BigDecimal)object2).scale()) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                bl = Boolean.TRUE;
            }
        } else {
            bl = and__5236__auto__9023 ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$equals_with_strict_scale.invokeStatic(object3, object4);
    }
}


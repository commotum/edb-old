/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Symbol
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Symbol;

public final class config$fn__822
extends AFunction {
    public static final AFn const__0 = (AFn)Symbol.intern((String)"datomic.aws-monitor", (String)"cloudwatch-reporter");
    public static final AFn const__1 = (AFn)Symbol.intern((String)"clojure.core", (String)"identity");

    public static Object invokeStatic(Object props) {
        Object object;
        Object and__5236__auto__825;
        Object object2 = and__5236__auto__825 = ((IFn)props).invoke((Object)"datomic.cloudwatchName");
        if (object2 != null && object2 != Boolean.FALSE) {
            Object and__5236__auto__824;
            Object object3 = and__5236__auto__824 = ((IFn)props).invoke((Object)"datomic.cloudwatchDimension");
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = props;
                props = null;
                object = ((IFn)object4).invoke((Object)"datomic.cloudwatchRegion");
            } else {
                object = and__5236__auto__824;
                Object var2_2 = null;
            }
        } else {
            object = and__5236__auto__825;
            Object var1_1 = null;
        }
        return object != null && object != Boolean.FALSE ? const__0 : const__1;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return config$fn__822.invokeStatic(object2);
    }
}


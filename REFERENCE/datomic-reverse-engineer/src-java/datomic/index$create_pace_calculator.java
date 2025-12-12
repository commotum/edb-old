/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$create_pace_calculator$fn__15310;
import datomic.index$create_pace_calculator$next_state__15307;

public final class index$create_pace_calculator
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final AFn const__5 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"window"), 0L, RT.keyword(null, (String)"work"), 0L, RT.keyword(null, (String)"msec"), 0L});

    public static Object invokeStatic(Object target_work, Object window_msec) {
        long start = System.currentTimeMillis();
        Object state2 = ((IFn)const__0.getRawRoot()).invoke((Object)const__5);
        index$create_pace_calculator$next_state__15307 next_state = new index$create_pace_calculator$next_state__15307(window_msec, start);
        Object object = target_work;
        target_work = null;
        index$create_pace_calculator$next_state__15307 index$create_pace_calculator$next_state__15307 = next_state;
        next_state = null;
        Object object2 = window_msec;
        window_msec = null;
        Object object3 = state2;
        state2 = null;
        return new index$create_pace_calculator$fn__15310(object, (Object)index$create_pace_calculator$next_state__15307, object2, object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$create_pace_calculator.invokeStatic(object3, object4);
    }
}


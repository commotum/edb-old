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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class aggregation$median
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"odd?");

    public static Object invokeStatic(Object coll) {
        Object object;
        Object object2 = coll;
        coll = null;
        ArrayList ls = new ArrayList((Collection)object2);
        Collections.sort(ls);
        int cnt = ls.size();
        long mid = (long)cnt / 2L;
        Object object3 = ((IFn)const__2.getRawRoot()).invoke((Object)cnt);
        if (object3 != null && object3 != Boolean.FALSE) {
            ArrayList arrayList = ls;
            ls = null;
            object = arrayList.get(RT.intCast((long)mid));
        } else {
            Object e = ls.get(RT.intCast((long)mid));
            ArrayList arrayList = ls;
            ls = null;
            object = Numbers.quotient((Object)Numbers.add(e, arrayList.get(RT.intCast((long)Numbers.dec((long)mid)))), (long)2L);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aggregation$median.invokeStatic(object2);
    }
}


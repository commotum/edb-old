/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$seg_last_datom
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"getx-uncached");

    public static Object invokeStatic(Object olookup, Object segid) {
        Object seg_data;
        Object object = olookup;
        olookup = null;
        Object object2 = segid;
        segid = null;
        Object object3 = seg_data = ((IFn)const__0.getRawRoot()).invoke(object, object2);
        Object object4 = seg_data;
        seg_data = null;
        return RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)((long)RT.count((Object)object4) - 1L)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$seg_last_datom.invokeStatic(object3, object4);
    }
}


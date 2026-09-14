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

public final class garbage$pace_gc
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"property");

    public static Object invokeStatic() {
        Object v3;
        Object temp__5457__auto__19834;
        Object object = temp__5457__auto__19834 = ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.gcStoragePaceMsec");
        if (object != null && object != Boolean.FALSE) {
            Object pace;
            Object object2 = temp__5457__auto__19834;
            temp__5457__auto__19834 = null;
            Object object3 = pace = object2;
            pace = null;
            Thread.sleep(RT.longCast((Object)((Number)object3)));
            v3 = null;
        } else {
            v3 = null;
        }
        return v3;
    }

    public Object invoke() {
        return garbage$pace_gc.invokeStatic();
    }
}


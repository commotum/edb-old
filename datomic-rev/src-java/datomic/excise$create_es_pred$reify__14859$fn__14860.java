/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.excise.ExcisePred;

public final class excise$create_es_pred$reify__14859$fn__14860
extends AFunction {
    Object d;
    private static Class __cached_class__0;
    public static final Var const__0;

    public excise$create_es_pred$reify__14859$fn__14860(Object object) {
        this.d = object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object p1__14858_SHARP_) {
        Object object;
        Object object2 = p1__14858_SHARP_;
        p1__14858_SHARP_ = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof ExcisePred) {
                object = ((ExcisePred)object3).ep_remove_QMARK_(this_.d);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        excise$create_es_pred$reify__14859$fn__14860 this_ = null;
        object = const__0.getRawRoot().invoke(object3, this_.d);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.excise", (String)"ep-remove?");
    }
}


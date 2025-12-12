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

public final class common$map_from_env$fn__9050
extends AFunction {
    Object env;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"env-key->clj-key");

    public common$map_from_env$fn__9050(Object object) {
        this.env = object;
    }

    public Object invoke(Object m, Object k) {
        Object object;
        Object temp__5455__auto__9052;
        Object object2 = temp__5455__auto__9052 = RT.get((Object)this_.env, (Object)k);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5455__auto__9052;
            temp__5455__auto__9052 = null;
            Object v = object3;
            Object object4 = m;
            m = null;
            Object object5 = k;
            k = null;
            Object object6 = v;
            v = null;
            common$map_from_env$fn__9050 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object4, ((IFn)const__2.getRawRoot()).invoke(object5), object6);
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}


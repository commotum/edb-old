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
import datomic.datafy.ObjectToData;

public final class datafy$object_to_data_wrapper
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object x) {
        Object object;
        Object object2 = x;
        x = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof ObjectToData) {
                object = ((ObjectToData)object3).object_to_data();
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        object = const__0.getRawRoot().invoke(object3);
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$object_to_data_wrapper.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.datafy", (String)"object-to-data");
    }
}


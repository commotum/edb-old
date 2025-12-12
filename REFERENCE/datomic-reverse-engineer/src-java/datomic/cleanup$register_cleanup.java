/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.cleanup.Manager;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;

public final class cleanup$register_cleanup
extends AFunction {
    public static Object invokeStatic(Object manager, Object object, Object cleanup2) {
        Object object2 = manager;
        manager = null;
        Object object3 = cleanup2;
        cleanup2 = null;
        ((Map)((Manager)manager).phantoms).put(new PhantomReference<Object>(object, (ReferenceQueue)((Manager)object2).queue), object3);
        Object var1_1 = null;
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cleanup$register_cleanup.invokeStatic(object4, object5, object6);
    }
}


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

public final class datalog$fn__18158
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"join-project-coll-with");

    public static Object invokeStatic(Object xs, Object ys, Object join_map, Object project_map_x, Object project_map_y, Object predctor) {
        Object object = xs;
        xs = null;
        Object object2 = ys;
        ys = null;
        Object object3 = join_map;
        join_map = null;
        Object object4 = project_map_x;
        project_map_x = null;
        Object object5 = project_map_y;
        project_map_y = null;
        Object object6 = predctor;
        predctor = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, object3, object4, object5, object6);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return datalog$fn__18158.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog.IJoin;

public final class datalog$join_project_coll
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object xs, Object ys, Object join_map, Object project_map_x, Object project_map_y, Object predctor) {
        Object object;
        Object object2 = ys;
        ys = null;
        Object object3 = object2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object3 instanceof IJoin) {
                Object object4 = xs;
                xs = null;
                Object object5 = ((IFn)const__2.getRawRoot()).invoke(join_map);
                Object object6 = join_map;
                join_map = null;
                Object object7 = project_map_y;
                project_map_y = null;
                Object object8 = project_map_x;
                project_map_x = null;
                Object object9 = predctor;
                predctor = null;
                object = ((IJoin)object3).join_project_with(object4, ((IFn)const__1.getRawRoot()).invoke(object5, ((IFn)const__3.getRawRoot()).invoke(object6)), object7, object8, object9);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object10 = xs;
        xs = null;
        Object object11 = ((IFn)const__2.getRawRoot()).invoke(join_map);
        Object object12 = join_map;
        join_map = null;
        Object object13 = project_map_y;
        project_map_y = null;
        Object object14 = project_map_x;
        project_map_x = null;
        Object object15 = predctor;
        predctor = null;
        object = const__0.getRawRoot().invoke(object3, object10, ((IFn)const__1.getRawRoot()).invoke(object11, ((IFn)const__3.getRawRoot()).invoke(object12)), object13, object14, object15);
        return object;
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
        return datalog$join_project_coll.invokeStatic(object7, object8, object9, object10, object11, object12);
    }

    static {
        const__0 = RT.var((String)"datomic.datalog", (String)"join-project-with");
        const__1 = RT.var((String)"clojure.core", (String)"zipmap");
        const__2 = RT.var((String)"clojure.core", (String)"vals");
        const__3 = RT.var((String)"clojure.core", (String)"keys");
    }
}


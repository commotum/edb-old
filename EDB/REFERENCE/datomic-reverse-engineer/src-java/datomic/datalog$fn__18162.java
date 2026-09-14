/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Map;

public final class datalog$fn__18162
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"join-project-coll-with");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"else");
    public static final Var const__6 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__7 = RT.keyword((String)"db.error", (String)"invalid-data-source");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__9 = RT.keyword(null, (String)"input");

    public static Object invokeStatic(Object xs, Object ys, Object join_map, Object project_map_x, Object project_map_y, Object predctor) {
        Object object;
        if (xs instanceof Iterable) {
            Object object2 = xs;
            xs = null;
            Object object3 = ys;
            ys = null;
            Object object4 = join_map;
            join_map = null;
            Object object5 = project_map_x;
            project_map_x = null;
            Object object6 = project_map_y;
            project_map_y = null;
            Object object7 = predctor;
            predctor = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object2, object3, object4, object5, object6, object7);
        } else if (xs instanceof Map) {
            Object object8 = xs;
            xs = null;
            Object object9 = ys;
            ys = null;
            Object object10 = join_map;
            join_map = null;
            Object object11 = project_map_x;
            project_map_x = null;
            Object object12 = project_map_y;
            project_map_y = null;
            Object object13 = predctor;
            predctor = null;
            object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object8), object9, object10, object11, object12, object13);
        } else {
            Keyword keyword = const__5;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object14 = ((IFn)const__8.getRawRoot()).invoke(xs.getClass(), (Object)" is not a valid data source type.");
                Object[] objectArray = new Object[2];
                objectArray[0] = const__9;
                Object object15 = xs;
                xs = null;
                objectArray[1] = object15;
                object = ((IFn)const__6.getRawRoot()).invoke((Object)const__7, object14, (Object)RT.mapUniqueKeys((Object[])objectArray));
            } else {
                object = null;
            }
        }
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
        return datalog$fn__18162.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$create_join_maps$m__18382;

public final class datalog$create_join_maps
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"range");

    public static Object invokeStatic(Object xbinds, Object ybinds, Object zbinds) {
        Object object = xbinds;
        xbinds = null;
        Object xb = ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke());
        Object yb = ((IFn)const__0.getRawRoot()).invoke(ybinds, ((IFn)const__1.getRawRoot()).invoke());
        Object zb = ((IFn)const__0.getRawRoot()).invoke(zbinds, ((IFn)const__1.getRawRoot()).invoke());
        datalog$create_join_maps$m__18382 m = new datalog$create_join_maps$m__18382();
        Object object2 = ybinds;
        ybinds = null;
        Object join_map = ((IFn)m).invoke(xb, yb, object2);
        Object object3 = xb;
        xb = null;
        Object proj_x = ((IFn)m).invoke(object3, zb, zbinds);
        datalog$create_join_maps$m__18382 datalog$create_join_maps$m__18382 = m;
        m = null;
        Object object4 = yb;
        yb = null;
        Object object5 = zb;
        zb = null;
        Object object6 = zbinds;
        zbinds = null;
        Object proj_y = ((IFn)datalog$create_join_maps$m__18382).invoke(object4, object5, object6);
        Object object7 = join_map;
        join_map = null;
        Object object8 = proj_x;
        proj_x = null;
        Object object9 = proj_y;
        proj_y = null;
        return Tuple.create((Object)object7, (Object)object8, (Object)object9);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datalog$create_join_maps.invokeStatic(object4, object5, object6);
    }
}


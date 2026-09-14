/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog.IJoin;

public final class datalog$fn__18062$G__18056__18069
extends AFunction {
    public Object invoke(Object gf__xs__18063, Object gf__ys__18064, Object gf__join_map__18065, Object gf__project_map_x__18066, Object gf__project_map_y__18067, Object gf__predctor__18068) {
        Object object = gf__xs__18063;
        gf__xs__18063 = null;
        Object object2 = gf__ys__18064;
        gf__ys__18064 = null;
        Object object3 = gf__join_map__18065;
        gf__join_map__18065 = null;
        Object object4 = gf__project_map_x__18066;
        gf__project_map_x__18066 = null;
        Object object5 = gf__project_map_y__18067;
        gf__project_map_y__18067 = null;
        Object object6 = gf__predctor__18068;
        gf__predctor__18068 = null;
        return ((IJoin)object).join_project(object2, object3, object4, object5, object6);
    }
}


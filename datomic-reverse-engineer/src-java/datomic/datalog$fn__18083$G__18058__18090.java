/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog.IJoin;

public final class datalog$fn__18083$G__18058__18090
extends AFunction {
    public Object invoke(Object gf__ys__18084, Object gf__xs__18085, Object gf__join_map__18086, Object gf__project_map_x__18087, Object gf__project_map_y__18088, Object gf__predctor__18089) {
        Object object = gf__ys__18084;
        gf__ys__18084 = null;
        Object object2 = gf__xs__18085;
        gf__xs__18085 = null;
        Object object3 = gf__join_map__18086;
        gf__join_map__18086 = null;
        Object object4 = gf__project_map_x__18087;
        gf__project_map_x__18087 = null;
        Object object5 = gf__project_map_y__18088;
        gf__project_map_y__18088 = null;
        Object object6 = gf__predctor__18089;
        gf__predctor__18089 = null;
        return ((IJoin)object).join_project_with(object2, object3, object4, object5, object6);
    }
}


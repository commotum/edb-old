/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.datalog.ExtRel;

public final class datalog$fn__18185$G__18181__18190
extends AFunction {
    public Object invoke(Object gf__src__18186, Object gf__consts__18187, Object gf__starts__18188, Object gf__whiles__18189) {
        Object object = gf__src__18186;
        gf__src__18186 = null;
        Object object2 = gf__consts__18187;
        gf__consts__18187 = null;
        Object object3 = gf__starts__18188;
        gf__starts__18188 = null;
        Object object4 = gf__whiles__18189;
        gf__whiles__18189 = null;
        return ((ExtRel)object).extrel(object2, object3, object4);
    }
}


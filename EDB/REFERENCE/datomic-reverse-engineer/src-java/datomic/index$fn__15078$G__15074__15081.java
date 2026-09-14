/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.index.IIndex;

public final class index$fn__15078$G__15074__15081
extends AFunction {
    public Object invoke(Object gf__idx__15079, Object gf__k__15080) {
        Object object = gf__idx__15079;
        gf__idx__15079 = null;
        Object object2 = gf__k__15080;
        gf__k__15080 = null;
        return ((IIndex)object).seek_seg(object2);
    }
}


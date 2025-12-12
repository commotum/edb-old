/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.LogSeek;

public final class log$fn__15961$G__15944__15964
extends AFunction {
    public Object invoke(Object gf__log__15962, Object gf__t__15963) {
        Object object = gf__log__15962;
        gf__log__15962 = null;
        Object object2 = gf__t__15963;
        gf__t__15963 = null;
        return ((LogSeek)object).seek_seg_path(object2);
    }
}


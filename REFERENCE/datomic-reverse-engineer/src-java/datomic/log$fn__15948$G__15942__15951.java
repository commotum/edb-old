/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.LogSeek;

public final class log$fn__15948$G__15942__15951
extends AFunction {
    public Object invoke(Object gf__log__15949, Object gf__t__15950) {
        Object object = gf__log__15949;
        gf__log__15949 = null;
        Object object2 = gf__t__15950;
        gf__t__15950 = null;
        return ((LogSeek)object).seek_tx_impl(object2);
    }
}


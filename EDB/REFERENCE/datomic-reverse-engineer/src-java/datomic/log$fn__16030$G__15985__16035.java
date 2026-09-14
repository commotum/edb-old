/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.Log;

public final class log$fn__16030$G__15985__16035
extends AFunction {
    public Object invoke(Object gf__log__16031, Object gf__cs__16032, Object gf__root_id__16033, Object gf__t__16034) {
        Object object = gf__log__16031;
        gf__log__16031 = null;
        Object object2 = gf__cs__16032;
        gf__cs__16032 = null;
        Object object3 = gf__root_id__16033;
        gf__root_id__16033 = null;
        Object object4 = gf__t__16034;
        gf__t__16034 = null;
        return ((Log)object).adopt_root(object2, object3, object4);
    }
}


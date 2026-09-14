/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.log.Log;

public final class log$fn__15993$G__15987__15997
extends AFunction {
    public Object invoke(Object gf__log__15994, Object gf__cs__15995, Object gf__msgs__15996) {
        Object object = gf__log__15994;
        gf__log__15994 = null;
        Object object2 = gf__cs__15995;
        gf__cs__15995 = null;
        Object object3 = gf__msgs__15996;
        gf__msgs__15996 = null;
        return ((Log)object).append(object2, object3);
    }
}


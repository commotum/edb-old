/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.process.CriticalFailure;

public final class process$fn__14896$G__14892__14902
extends AFunction {
    public Object invoke(Object gf_____14899, Object gf__msg__14900, Object gf__t__14901) {
        Object object = gf_____14899;
        gf_____14899 = null;
        Object object2 = gf__msg__14900;
        gf__msg__14900 = null;
        Object object3 = gf__t__14901;
        gf__t__14901 = null;
        return ((CriticalFailure)object).fail(object2, object3);
    }

    public Object invoke(Object gf_____14897, Object gf__msg__14898) {
        Object object = gf_____14897;
        gf_____14897 = null;
        Object object2 = gf__msg__14898;
        gf__msg__14898 = null;
        return ((CriticalFailure)object).fail(object2);
    }
}


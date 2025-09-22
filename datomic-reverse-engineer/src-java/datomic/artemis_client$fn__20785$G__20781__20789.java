/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.artemis_client.HornetImpl;

public final class artemis_client$fn__20785$G__20781__20789
extends AFunction {
    public Object invoke(Object gf_____20786, Object gf__creds__20787, Object gf__args__20788) {
        Object object = gf_____20786;
        gf_____20786 = null;
        Object object2 = gf__creds__20787;
        gf__creds__20787 = null;
        Object object3 = gf__args__20788;
        gf__args__20788 = null;
        return ((HornetImpl)object).start_session_STAR_(object2, object3);
    }
}


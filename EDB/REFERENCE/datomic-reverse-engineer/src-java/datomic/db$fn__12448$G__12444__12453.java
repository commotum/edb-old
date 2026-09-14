/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.LocalizeTempid;

public final class db$fn__12448$G__12444__12453
extends AFunction {
    public Object invoke(Object gf_____12449, Object gf__db__12450, Object gf__procargs__12451, Object gf__local_tempids__12452) {
        Object object = gf_____12449;
        gf_____12449 = null;
        Object object2 = gf__db__12450;
        gf__db__12450 = null;
        Object object3 = gf__procargs__12451;
        gf__procargs__12451 = null;
        Object object4 = gf__local_tempids__12452;
        gf__local_tempids__12452 = null;
        return ((LocalizeTempid)object).local_id(object2, object3, object4);
    }
}


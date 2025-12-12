/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import datomic.db.PartitionRequests;
import java.util.HashMap;

public final class db$part_requests
extends AFunction {
    public static Object invokeStatic() {
        return new PartitionRequests(new HashMap(), new HashMap());
    }

    public Object invoke() {
        return db$part_requests.invokeStatic();
    }
}


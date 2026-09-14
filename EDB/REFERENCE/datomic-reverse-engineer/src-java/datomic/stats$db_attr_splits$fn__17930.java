/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Tuple;

public final class stats$db_attr_splits$fn__17930
extends AFunction {
    public Object invoke(Object p__17929) {
        Object vec__17937;
        Object object = p__17929;
        p__17929 = null;
        Object vec__17931 = object;
        Object vec__17934 = RT.nth((Object)vec__17931, (int)RT.intCast((long)0L), null);
        Object e1 = RT.nth((Object)vec__17934, (int)RT.intCast((long)0L), null);
        Object object2 = vec__17934;
        vec__17934 = null;
        Object ct = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
        Object object3 = vec__17931;
        vec__17931 = null;
        Object object4 = vec__17937 = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        vec__17937 = null;
        Object e2 = RT.nth((Object)object4, (int)RT.intCast((long)0L), null);
        Object object5 = e1;
        e1 = null;
        Object object6 = e2;
        e2 = null;
        Object object7 = ct;
        ct = null;
        return Tuple.create((Object)object5, (Object)object6, (Object)object7);
    }
}


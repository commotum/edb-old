/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import datomic.query.EntityMap;

public final class query$emap
extends AFunction {
    public static final Keyword const__0 = RT.keyword((String)"db", (String)"id");

    public static Object invokeStatic(Object db2, Object eid) {
        Object object = db2;
        db2 = null;
        Object object2 = eid;
        Object[] objectArray = new Object[2];
        objectArray[0] = const__0;
        Object object3 = eid;
        eid = null;
        objectArray[1] = object3;
        return new EntityMap(object, object2, RT.mapUniqueKeys((Object[])objectArray), null);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$emap.invokeStatic(object3, object4);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cassandra_values_v4$get_value$fn__10319$fn__10323$fn__10324;

public final class cassandra_values_v4$get_value$fn__10319$fn__10323
extends AFunction {
    Object id;
    Object session;
    Object p1__10312_SHARP_;
    Object table;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.cassandra-values-v4", (String)"*retry*");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public cassandra_values_v4$get_value$fn__10319$fn__10323(Object object, Object object2, Object object3, Object object4) {
        this.id = object;
        this.session = object2;
        this.p1__10312_SHARP_ = object3;
        this.table = object4;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.p1__10312_SHARP_ = null;
            objectArray[1] = ((IFn)const__1.get()).invoke((Object)new cassandra_values_v4$get_value$fn__10319$fn__10323$fn__10324(this.id, this.session, this.p1__10312_SHARP_, this.table));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__2;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}


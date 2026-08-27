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
import datomic.cassandra_values$get_value$fn__20561$fn__20565$fn__20566;

public final class cassandra_values$get_value$fn__20561$fn__20565
extends AFunction {
    Object session;
    Object id;
    Object table;
    Object p1__20554_SHARP_;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.cassandra-values", (String)"*retry*");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public cassandra_values$get_value$fn__20561$fn__20565(Object object, Object object2, Object object3, Object object4) {
        this.session = object;
        this.id = object2;
        this.table = object3;
        this.p1__20554_SHARP_ = object4;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.p1__20554_SHARP_ = null;
            objectArray[1] = ((IFn)const__1.get()).invoke((Object)new cassandra_values$get_value$fn__20561$fn__20565$fn__20566(this.session, this.id, this.table, this.p1__20554_SHARP_));
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


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
import datomic.ddb_values$get_value$fn__20431$fn__20435$fn__20436;

public final class ddb_values$get_value$fn__20431$fn__20435
extends AFunction {
    Object ddb_client;
    Object table;
    Object p1__20420_SHARP_;
    Object id;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.ddb-values", (String)"*retry*");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public ddb_values$get_value$fn__20431$fn__20435(Object object, Object object2, Object object3, Object object4) {
        this.ddb_client = object;
        this.table = object2;
        this.p1__20420_SHARP_ = object3;
        this.id = object4;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.p1__20420_SHARP_ = null;
            objectArray[1] = ((IFn)const__1.get()).invoke((Object)new ddb_values$get_value$fn__20431$fn__20435$fn__20436(this.ddb_client, this.table, this.p1__20420_SHARP_, this.id));
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


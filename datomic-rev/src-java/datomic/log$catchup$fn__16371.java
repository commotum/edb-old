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

public final class log$catchup$fn__16371
extends AFunction {
    Object log_txes;
    Object db;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"add-fulltext");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Keyword const__3 = RT.keyword(null, (String)"data");
    public static final Keyword const__4 = RT.keyword(null, (String)"threw");

    public log$catchup$fn__16371(Object object, Object object2) {
        this.log_txes = object;
        this.db = object2;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            iPersistentMap = RT.mapUniqueKeys((Object[])new Object[]{const__0, ((IFn)const__1.getRawRoot()).invoke(this.db, ((IFn)const__2.getRawRoot()).invoke((Object)const__3, this.log_txes))});
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}


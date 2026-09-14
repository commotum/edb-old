/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$add_fulltext$fn__14059$fn__14060;

public final class db$add_fulltext$fn__14059
extends AFunction {
    Object db;
    Object data;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"update-in");
    public static final AFn const__4 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"memidx"), (Object)RT.keyword(null, (String)"fulltext"));
    public static final Keyword const__5 = RT.keyword(null, (String)"threw");

    public db$add_fulltext$fn__14059(Object object, Object object2) {
        this.db = object;
        this.data = object2;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.db = null;
            this.data = null;
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(this.db, (Object)const__4, (Object)new db$add_fulltext$fn__14059$fn__14060(this.db, this.data));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__5;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}


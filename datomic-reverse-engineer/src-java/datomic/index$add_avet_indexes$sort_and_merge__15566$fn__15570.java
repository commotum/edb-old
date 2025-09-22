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
import datomic.index$add_avet_indexes$sort_and_merge__15566$fn__15570$fn__15571;

public final class index$add_avet_indexes$sort_and_merge__15566$fn__15570
extends AFunction {
    Object root_map;
    Object olookup;
    Object as_of_t;
    Object cstore;
    Object k;
    Object garbage;
    Object db;
    Object aevt_datoms;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"avet-sort-and-process-datoms");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public index$add_avet_indexes$sort_and_merge__15566$fn__15570(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.root_map = object;
        this.olookup = object2;
        this.as_of_t = object3;
        this.cstore = object4;
        this.k = object5;
        this.garbage = object6;
        this.db = object7;
        this.aevt_datoms = object8;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.aevt_datoms = null;
            this.root_map = null;
            this.olookup = null;
            this.as_of_t = null;
            this.cstore = null;
            this.k = null;
            this.garbage = null;
            this.db = null;
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(this.aevt_datoms, (Object)new index$add_avet_indexes$sort_and_merge__15566$fn__15570$fn__15571(this.root_map, this.olookup, this.as_of_t, this.cstore, this.k, this.garbage, this.db));
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


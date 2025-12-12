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

public final class index$merge_db_STAR_$build_tiered_index__15765$fn__15774
extends AFunction {
    Object mem_idx;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"mem-index-bytes");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public index$merge_db_STAR_$build_tiered_index__15765$fn__15774(Object object) {
        this.mem_idx = object;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.mem_idx = null;
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(this.mem_idx);
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


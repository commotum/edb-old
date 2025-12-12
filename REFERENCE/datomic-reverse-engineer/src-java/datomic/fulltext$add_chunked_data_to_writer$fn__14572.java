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

public final class fulltext$add_chunked_data_to_writer$fn__14572
extends AFunction {
    Object writer;
    Object datums;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.lucene", (String)"add-documents");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pmap");
    public static final Var const__3 = RT.var((String)"datomic.fulltext-index", (String)"datum->doc");
    public static final Keyword const__4 = RT.keyword(null, (String)"threw");

    public fulltext$add_chunked_data_to_writer$fn__14572(Object object, Object object2) {
        this.writer = object;
        this.datums = object2;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            iPersistentMap = RT.mapUniqueKeys((Object[])new Object[]{const__0, ((IFn)const__1.getRawRoot()).invoke(this.writer, ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), this.datums))});
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


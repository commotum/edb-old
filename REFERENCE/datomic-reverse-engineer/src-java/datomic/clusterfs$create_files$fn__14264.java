/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.clusterfs$create_files$fn__14264$fn__14266;

public final class clusterfs$create_files$fn__14264
extends AFunction {
    Object cs;
    Object files;
    Object chunk_size;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public clusterfs$create_files$fn__14264(Object object, Object object2, Object object3) {
        this.cs = object;
        this.files = object2;
        this.chunk_size = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.cs = null;
            this.chunk_size = null;
            this.files = null;
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke((Object)new clusterfs$create_files$fn__14264$fn__14266(this.cs, this.chunk_size), (Object)PersistentArrayMap.EMPTY, this.files);
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


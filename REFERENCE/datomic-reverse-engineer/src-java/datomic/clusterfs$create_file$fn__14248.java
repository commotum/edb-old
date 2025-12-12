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
import datomic.clusterfs$create_file$fn__14248$fn__14249;
import java.io.File;
import java.io.FileInputStream;

public final class clusterfs$create_file$fn__14248
extends AFunction {
    Object base;
    Object tailsize;
    Object local_file;
    Object prefix;
    Object chunk_size;
    Object cs;
    long fullchunks;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"clojure.java.io", (String)"file");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public clusterfs$create_file$fn__14248(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, long l) {
        this.base = object;
        this.tailsize = object2;
        this.local_file = object3;
        this.prefix = object4;
        this.chunk_size = object5;
        this.cs = object6;
        this.fullchunks = l;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.local_file = null;
            FileInputStream s = new FileInputStream((File)((IFn)const__1.getRawRoot()).invoke(this.local_file));
            this.base = null;
            this.tailsize = null;
            this.prefix = null;
            this.chunk_size = null;
            FileInputStream fileInputStream = s;
            s = null;
            this.cs = null;
            objectArray[1] = ((IFn)new clusterfs$create_file$fn__14248$fn__14249(this.base, this.tailsize, this.prefix, this.chunk_size, fileInputStream, this.cs, this.fullchunks)).invoke();
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


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.clusterfs$create_file$fn__14248$fn__14249$fn__14250;
import java.io.FileInputStream;

public final class clusterfs$create_file$fn__14248$fn__14249
extends AFunction {
    Object base;
    Object tailsize;
    Object prefix;
    Object chunk_size;
    Object s;
    Object cs;
    long fullchunks;
    public static final Var const__0 = RT.var((String)"datomic.cluster", (String)"write-vals");
    public static final Keyword const__1 = RT.keyword(null, (String)"clusterfs");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__5 = RT.var((String)"datomic.clusterfs", (String)"chunk-path");
    public static final Var const__6 = RT.var((String)"datomic.clusterfs", (String)"fressian-chunk-from-channel");

    public clusterfs$create_file$fn__14248$fn__14249(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, long l) {
        this.base = object;
        this.tailsize = object2;
        this.prefix = object3;
        this.chunk_size = object4;
        this.s = object5;
        this.cs = object6;
        this.fullchunks = l;
    }

    public Object invoke() {
        Object object;
        try {
            this.chunk_size = null;
            ((IFn)const__0.getRawRoot()).invoke(this.cs, (Object)const__1, ((IFn)const__2.getRawRoot()).invoke((Object)new clusterfs$create_file$fn__14248$fn__14249$fn__14250(this.prefix, this.chunk_size, this.s), ((IFn)const__3.getRawRoot()).invoke((Object)Numbers.num((long)this.fullchunks))));
            if (Numbers.isZero((Object)this.tailsize)) {
            } else {
                this.cs = null;
                Object[] objectArray = new Object[2];
                this.prefix = null;
                objectArray[0] = ((IFn)const__5.getRawRoot()).invoke(this.prefix, (Object)Numbers.num((long)this.fullchunks));
                this.tailsize = null;
                objectArray[1] = ((IFn)const__6.getRawRoot()).invoke((Object)((FileInputStream)this.s).getChannel(), this.tailsize);
                ((IFn)const__0.getRawRoot()).invoke(this.cs, (Object)const__1, (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
            object = this.base = null;
        }
        finally {
            this.s = null;
            ((FileInputStream)this.s).close();
        }
        return object;
    }
}


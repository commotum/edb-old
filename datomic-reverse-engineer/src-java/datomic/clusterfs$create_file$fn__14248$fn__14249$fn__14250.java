/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.io.FileInputStream;

public final class clusterfs$create_file$fn__14248$fn__14249$fn__14250
extends AFunction {
    Object prefix;
    Object chunk_size;
    Object s;
    public static final Var const__0 = RT.var((String)"datomic.clusterfs", (String)"chunk-path");
    public static final Var const__1 = RT.var((String)"datomic.clusterfs", (String)"fressian-chunk-from-channel");

    public clusterfs$create_file$fn__14248$fn__14249$fn__14250(Object object, Object object2, Object object3) {
        this.prefix = object;
        this.chunk_size = object2;
        this.s = object3;
    }

    public Object invoke(Object chunkno) {
        Object object = chunkno;
        chunkno = null;
        return Tuple.create((Object)((IFn)const__0.getRawRoot()).invoke(this.prefix, object), (Object)((IFn)const__1.getRawRoot()).invoke((Object)((FileInputStream)this.s).getChannel(), this.chunk_size));
    }
}


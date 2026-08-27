/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.clusterfs.ClusterFS;

public final class clusterfs$file_chunk_keys
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.clusterfs", (String)"ceil");
    public static final Var const__2 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"get-in");
    public static final Keyword const__4 = RT.keyword(null, (String)"base");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__7 = RT.var((String)"datomic.clusterfs", (String)"chunk-path");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"range");

    public static Object invokeStatic(Object clusterfs2, Object file) {
        Object prefix;
        long length = ((ClusterFS)clusterfs2).fileLength((String)file);
        long n = ((IFn.OL)const__0.getRawRoot()).invokePrim((Object)Numbers.divide((long)length, (long)((ClusterFS)clusterfs2).chunkSize()));
        Object object = clusterfs2;
        clusterfs2 = null;
        Object object2 = file;
        file = null;
        Object object3 = prefix = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(((ClusterFS)object).dir, (Object)Tuple.create((Object)object2, (Object)const__4)));
        prefix = null;
        return ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), object3), ((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)n)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return clusterfs$file_chunk_keys.invokeStatic(object3, object4);
    }
}


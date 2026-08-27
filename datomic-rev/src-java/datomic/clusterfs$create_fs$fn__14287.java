/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.clusterfs.ClusterFS;
import org.slf4j.LoggerFactory;

public final class clusterfs$create_fs$fn__14287
extends AFunction {
    Object chunk_size;
    Object base;
    Object cs;
    Object dirid;
    Object files;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__9;
    public static final Var const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final Keyword const__15;
    public static final Var const__16;
    public static final Keyword const__17;

    public clusterfs$create_fs$fn__14287(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.chunk_size = object;
        this.base = object2;
        this.cs = object3;
        this.dirid = object4;
        this.files = object5;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = clusterfs$create_fs$fn__14287.const__0;
            this.base = null;
            this.files = null;
            v1 = dir = ((IFn)clusterfs$create_fs$fn__14287.const__1.getRawRoot()).invoke(this.base, ((IFn)clusterfs$create_fs$fn__14287.const__2.getRawRoot()).invoke(this.cs, this.files, (Object)clusterfs$create_fs$fn__14287.const__3, this.chunk_size));
            dir = null;
            this.chunk_size = null;
            cfs = new ClusterFS(v1, RT.intCast((Object)((Number)this.chunk_size)));
            v2 = this.cs;
            this.cs = null;
            v3 = v2;
            if (Util.classOf((Object)v2) == clusterfs$create_fs$fn__14287.__cached_class__0) ** GOTO lbl17
            if (!(v3 instanceof ClusteredStore)) {
                v3 = v3;
                clusterfs$create_fs$fn__14287.__cached_class__0 = Util.classOf((Object)v3);
lbl17:
                // 2 sources

                v4 = clusterfs$create_fs$fn__14287.const__4.getRawRoot().invoke(v3, ((IFn)clusterfs$create_fs$fn__14287.const__5.getRawRoot()).invoke(this.dirid), ((IFn)clusterfs$create_fs$fn__14287.const__6.getRawRoot()).invoke((Object)cfs, clusterfs$create_fs$fn__14287.const__7.getRawRoot()));
            } else {
                v4 = ((ClusteredStore)v3).create_val(((IFn)clusterfs$create_fs$fn__14287.const__5.getRawRoot()).invoke(this.dirid), ((IFn)clusterfs$create_fs$fn__14287.const__6.getRawRoot()).invoke((Object)cfs, clusterfs$create_fs$fn__14287.const__7.getRawRoot()));
            }
            result = v4;
            logger = LoggerFactory.getLogger((String)"datomic.clusterfs");
            if (logger.isInfoEnabled()) {
                v5 = logger;
                logger = null;
                v6 = cfs;
                cfs = null;
                v5.info((String)((IFn)clusterfs$create_fs$fn__14287.const__8.getRawRoot()).invoke(((IFn)clusterfs$create_fs$fn__14287.const__9.getRawRoot()).invoke(((IFn)clusterfs$create_fs$fn__14287.const__10.getRawRoot()).invoke((Object)v6), (Object)clusterfs$create_fs$fn__14287.const__11, (Object)clusterfs$create_fs$fn__14287.const__12, (Object)clusterfs$create_fs$fn__14287.const__13, this.dirid)));
            }
            v7 = result;
            result = null;
            if (Util.equiv((Object)clusterfs$create_fs$fn__14287.const__15, (Object)((IFn)clusterfs$create_fs$fn__14287.const__16.getRawRoot()).invoke(v7))) {
                v8 = this.dirid;
                this.dirid = null;
            } else {
                v8 = null;
            }
            v0[1] = v8;
            var5_7 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v9 = new Object[2];
            v9[0] = clusterfs$create_fs$fn__14287.const__17;
            t__8983__auto__ = null;
            v9[1] = t__8983__auto__;
            var5_7 = RT.mapUniqueKeys((Object[])v9);
        }
        return var5_7;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"clojure.core", (String)"merge");
        const__2 = RT.var((String)"datomic.clusterfs", (String)"create-files");
        const__3 = RT.keyword(null, (String)"chunk-size");
        const__4 = RT.var((String)"datomic.cluster", (String)"create-val");
        const__5 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__6 = RT.var((String)"datomic.fressian", (String)"fressian-val");
        const__7 = RT.var((String)"datomic.clusterfs", (String)"write-handlers");
        const__8 = RT.var((String)"datomic.slf4j", (String)"process");
        const__9 = RT.var((String)"clojure.core", (String)"assoc");
        const__10 = RT.var((String)"datomic.clusterfs", (String)"describe");
        const__11 = RT.keyword(null, (String)"event");
        const__12 = RT.keyword((String)"clusterfs", (String)"create-fs");
        const__13 = RT.keyword(null, (String)"uuid");
        const__15 = RT.keyword(null, (String)"created");
        const__16 = RT.var((String)"clojure.core", (String)"deref");
        const__17 = RT.keyword(null, (String)"threw");
    }
}


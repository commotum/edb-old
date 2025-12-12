/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.integrity$clusterfs_path_reachability$fn__22408$fn__22413;

public final class integrity$clusterfs_path_reachability$fn__22408
extends AFunction {
    Object cfs;
    Object olookup;
    Object progress;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"base");
    public static final Keyword const__7 = RT.keyword(null, (String)"length");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__9 = RT.var((String)"datomic.clusterfs", (String)"file-chunk-keys");

    public integrity$clusterfs_path_reachability$fn__22408(Object object, Object object2, Object object3) {
        this.cfs = object;
        this.olookup = object2;
        this.progress = object3;
    }

    public Object invoke(Object p__22407) {
        Object object;
        Object object2 = p__22407;
        p__22407 = null;
        Object vec__22409 = object2;
        Object filename = RT.nth((Object)vec__22409, (int)RT.intCast((long)0L), null);
        Object object3 = vec__22409;
        vec__22409 = null;
        Object map__22412 = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object object4 = ((IFn)const__3.getRawRoot()).invoke(map__22412);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__22412;
            map__22412 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object5)));
        } else {
            object = map__22412;
            map__22412 = null;
        }
        Object map__224122 = object;
        RT.get((Object)map__224122, (Object)const__6);
        Object object6 = map__224122;
        map__224122 = null;
        RT.get((Object)object6, (Object)const__7);
        integrity$clusterfs_path_reachability$fn__22408$fn__22413 integrity$clusterfs_path_reachability$fn__22408$fn__22413 = new integrity$clusterfs_path_reachability$fn__22408$fn__22413(filename, this_.olookup, this_.progress);
        Object object7 = filename;
        filename = null;
        integrity$clusterfs_path_reachability$fn__22408 this_ = null;
        return ((IFn)const__8.getRawRoot()).invoke((Object)integrity$clusterfs_path_reachability$fn__22408$fn__22413, ((IFn)const__9.getRawRoot()).invoke(this_.cfs, object7));
    }
}


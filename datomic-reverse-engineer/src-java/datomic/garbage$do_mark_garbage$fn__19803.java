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
import datomic.garbage$do_mark_garbage$fn__19803$fn__19804;

public final class garbage$do_mark_garbage$fn__19803
extends AFunction {
    Object cluster;
    Object max_dir_size;
    Object lookup;
    Object cluster_garbage;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.garbage.fressian", (String)"->GarbageLeaf");
    public static final Keyword const__2 = RT.keyword(null, (String)"threw");

    public garbage$do_mark_garbage$fn__19803(Object object, Object object2, Object object3, Object object4) {
        this.cluster = object;
        this.max_dir_size = object2;
        this.lookup = object3;
        this.cluster_garbage = object4;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object leaf;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object object = leaf = ((IFn)const__1.getRawRoot()).invoke(this.cluster_garbage);
            leaf = null;
            objectArray[1] = ((IFn)new garbage$do_mark_garbage$fn__19803$fn__19804(this.cluster, this.max_dir_size, this.lookup, object)).invoke();
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


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class garbage$gc_delete_vals$fn__19836
extends AFunction {
    Object cluster;
    private static Class __cached_class__0;
    public static final Var const__0;

    public garbage$gc_delete_vals$fn__19836(Object object) {
        this.cluster = object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object p1__19835_SHARP_) {
        Object object;
        Object object2 = this_.cluster;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = p1__19835_SHARP_;
                p1__19835_SHARP_ = null;
                object = ((ClusteredStore)object2).delete(object3);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object object4 = p1__19835_SHARP_;
        p1__19835_SHARP_ = null;
        garbage$gc_delete_vals$fn__19836 this_ = null;
        object = const__0.getRawRoot().invoke(object2, object4);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.cluster", (String)"delete");
    }
}


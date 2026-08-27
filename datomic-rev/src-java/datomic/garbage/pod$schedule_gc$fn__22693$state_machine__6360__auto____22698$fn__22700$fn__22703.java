/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.garbage;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700$fn__22703
extends AFunction {
    Object cluster;
    private static Class __cached_class__0;
    public static final Var const__0;

    public pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700$fn__22703(Object object) {
        this.cluster = object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object p1__22599_SHARP_) {
        Object object;
        Object object2 = this_.cluster;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = p1__22599_SHARP_;
                p1__22599_SHARP_ = null;
                object = ((ClusteredStore)object2).delete(object3);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object object4 = p1__22599_SHARP_;
        p1__22599_SHARP_ = null;
        pod$schedule_gc$fn__22693$state_machine__6360__auto____22698$fn__22700$fn__22703 this_ = null;
        object = const__0.getRawRoot().invoke(object2, object4);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.cluster", (String)"delete");
    }
}


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

public final class integrity$fulltext_path_reachability$fn__22418
extends AFunction {
    Object olookup;
    Object db;
    Object progress;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__6 = RT.var((String)"datomic.api", (String)"ident");
    public static final Var const__7 = RT.var((String)"datomic.integrity", (String)"clusterfs-path-reachability");

    public integrity$fulltext_path_reachability$fn__22418(Object object, Object object2, Object object3) {
        this.olookup = object;
        this.db = object2;
        this.progress = object3;
    }

    public Object invoke(Object p__22417) {
        Object object;
        Object temp__5455__auto__22423;
        Object object2 = p__22417;
        p__22417 = null;
        Object vec__22419 = object2;
        Object attrid = RT.nth((Object)vec__22419, (int)RT.intCast((long)0L), null);
        Object object3 = vec__22419;
        vec__22419 = null;
        Object uuid = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object object4 = temp__5455__auto__22423 = RT.get((Object)this_.olookup, (Object)((IFn)const__4.getRawRoot()).invoke(uuid));
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5455__auto__22423;
            temp__5455__auto__22423 = null;
            Object cfs = object5;
            Object object6 = attrid;
            attrid = null;
            Object object7 = uuid;
            uuid = null;
            Object object8 = cfs;
            cfs = null;
            integrity$fulltext_path_reachability$fn__22418 this_ = null;
            object = ((IFn)const__5.getRawRoot()).invoke((Object)Tuple.create((Object)((IFn)const__6.getRawRoot()).invoke(this_.db, object6), (Object)((IFn)const__4.getRawRoot()).invoke(object7), (Object)Boolean.TRUE), ((IFn)const__7.getRawRoot()).invoke(object8, this_.olookup, this_.progress));
        } else {
            Object object9 = attrid;
            attrid = null;
            Object object10 = uuid;
            uuid = null;
            object = Tuple.create((Object)((IFn)const__6.getRawRoot()).invoke(this_.db, object9), (Object)((IFn)const__4.getRawRoot()).invoke(object10), (Object)Boolean.FALSE);
        }
        return object;
    }
}


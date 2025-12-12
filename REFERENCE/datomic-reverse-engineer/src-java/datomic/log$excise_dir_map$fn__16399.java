/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class log$excise_dir_map$fn__16399
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"conj");

    public Object invoke(Object m, Object p__16398) {
        Object object;
        Object object2 = p__16398;
        p__16398 = null;
        Object vec__16400 = object2;
        Object dirid = RT.nth((Object)vec__16400, (int)RT.intCast((long)0L), null);
        Object object3 = vec__16400;
        vec__16400 = null;
        Object segid = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        Object object4 = ((IFn)const__3.getRawRoot()).invoke(RT.get((Object)m, (Object)dirid), segid);
        if (object4 != null && object4 != Boolean.FALSE) {
            object = m;
            m = null;
        } else {
            Object object5 = m;
            Object object6 = dirid;
            Object object7 = m;
            m = null;
            Object object8 = dirid;
            dirid = null;
            Object object9 = segid;
            segid = null;
            log$excise_dir_map$fn__16399 this_ = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object5, object6, ((IFn)const__6.getRawRoot()).invoke(RT.get((Object)object7, (Object)object8, (Object)PersistentHashSet.EMPTY), object9));
        }
        return object;
    }
}


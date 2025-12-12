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

public final class db$alter_attribute$fn__13220
extends AFunction {
    Object before;
    Object attr;
    Object eafter;
    Object ebefore;
    Object after;
    Object eid;
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"find-alter-fn");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"concat");

    public db$alter_attribute$fn__13220(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.before = object;
        this.attr = object2;
        this.eafter = object3;
        this.ebefore = object4;
        this.after = object5;
        this.eid = object6;
    }

    public Object invoke(Object p__13219, Object fid) {
        Object f;
        Object object = p__13219;
        p__13219 = null;
        Object vec__13221 = object;
        Object db2 = RT.nth((Object)vec__13221, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__13221;
        vec__13221 = null;
        Object prev_errors = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        Object vbefore = ((IFn)this.attr).invoke(this.before, this.ebefore, fid);
        Object vafter = ((IFn)this.attr).invoke(this.after, this.eafter, fid);
        Object object3 = fid;
        fid = null;
        Object object4 = f = ((IFn)const__3.getRawRoot()).invoke(this.before, this.eid, object3, vbefore, vafter);
        f = null;
        Object object5 = db2;
        db2 = null;
        Object object6 = vbefore;
        vbefore = null;
        Object object7 = vafter;
        vafter = null;
        Object vec__13224 = ((IFn)object4).invoke(object5, this.eid, object6, object7);
        Object db3 = RT.nth((Object)vec__13224, (int)RT.uncheckedIntCast((long)0L), null);
        Object object8 = vec__13224;
        vec__13224 = null;
        Object errors = RT.nth((Object)object8, (int)RT.uncheckedIntCast((long)1L), null);
        Object object9 = db3;
        db3 = null;
        Object object10 = errors;
        errors = null;
        Object object11 = prev_errors;
        prev_errors = null;
        return Tuple.create((Object)object9, (Object)((IFn)const__4.getRawRoot()).invoke(object10, object11));
    }
}


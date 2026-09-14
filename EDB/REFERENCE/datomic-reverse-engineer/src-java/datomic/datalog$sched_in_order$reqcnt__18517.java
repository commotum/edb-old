/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$sched_in_order$reqcnt__18517
extends AFunction {
    Object prog;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"find");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"meta");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"reqcnt"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public datalog$sched_in_order$reqcnt__18517(Object object) {
        this.prog = object;
    }

    public Object invoke(Object clause) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(this.prog, ((IFn)const__1.getRawRoot()).invoke(clause));
        if (object2 != null && object2 != Boolean.FALSE) {
            Object vec__18518;
            Object object3 = clause;
            clause = null;
            Object object4 = vec__18518 = ((IFn)const__2.getRawRoot()).invoke(this.prog, ((IFn)const__1.getRawRoot()).invoke(object3));
            vec__18518 = null;
            Object p = RT.nth((Object)object4, (int)RT.uncheckedIntCast((long)0L), null);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object5 = p;
            p = null;
            Object object6 = ((IFn)const__6.getRawRoot()).invoke(object5);
            object = iLookupThunk.get(object6);
            if (iLookupThunk == object) {
                __thunk__0__ = __site__0__.fault(object6);
                object = __thunk__0__.get(object6);
            }
        } else {
            object = null;
        }
        return object;
    }
}


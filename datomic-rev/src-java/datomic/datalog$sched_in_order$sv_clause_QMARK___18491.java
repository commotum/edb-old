/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$sched_in_order$sv_clause_QMARK___18491
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final AFn const__5 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"tuple"), RT.keyword(null, (String)"scalar")});
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"argvars"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"bind-type"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public Object invoke(Object p1__18445_SHARP_) {
        Object object;
        Object and__5236__auto__18494;
        Object object2 = and__5236__auto__18494 = ((IFn)const__0.getRawRoot()).invoke(p1__18445_SHARP_);
        if (object2 != null && object2 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = p1__18445_SHARP_;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            boolean and__5236__auto__18493 = Util.identical((Object)object4, null);
            if (and__5236__auto__18493) {
                IFn iFn = (IFn)const__5;
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object5 = p1__18445_SHARP_;
                p1__18445_SHARP_ = null;
                Object object6 = iLookupThunk2.get(object5);
                if (iLookupThunk2 == object6) {
                    __thunk__1__ = __site__1__.fault(object5);
                    object6 = __thunk__1__.get(object5);
                }
                datalog$sched_in_order$sv_clause_QMARK___18491 this_ = null;
                object = iFn.invoke(object6);
            } else {
                object = and__5236__auto__18493 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object = and__5236__auto__18494;
            Object var2_2 = null;
        }
        return object;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datalog$sched_in_order$cbinds__18489
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__4 = RT.var((String)"datomic.datalog", (String)"not-join-clause?");
    public static final Var const__5 = RT.var((String)"datomic.datalog", (String)"free-vars");
    public static final Keyword const__6 = RT.keyword(null, (String)"else");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__8 = RT.var((String)"datomic.datalog", (String)"variable?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"argvars"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"binds"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public Object invoke(Object clause) {
        Object object;
        datalog$sched_in_order$cbinds__18489 this_;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(clause);
        if (object2 != null && object2 != Boolean.FALSE) {
            IFn iFn = (IFn)const__1.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = clause;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object5 = clause;
            clause = null;
            Object object6 = iLookupThunk2.get(object5);
            if (iLookupThunk2 == object6) {
                __thunk__1__ = __site__1__.fault(object5);
                object6 = __thunk__1__.get(object5);
            }
            this_ = null;
            object = iFn.invoke(object4, object6);
        } else {
            Object object7 = ((IFn)const__4.getRawRoot()).invoke(clause);
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object8 = clause;
                clause = null;
                this_ = null;
                object = ((IFn)const__5.getRawRoot()).invoke(object8);
            } else {
                Keyword keyword = const__6;
                if (keyword != null && keyword != Boolean.FALSE) {
                    Object object9 = clause;
                    clause = null;
                    this_ = null;
                    object = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), object9);
                } else {
                    object = null;
                }
            }
        }
        return object;
    }
}


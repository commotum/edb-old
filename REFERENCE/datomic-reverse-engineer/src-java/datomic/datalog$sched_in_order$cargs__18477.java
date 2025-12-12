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

public final class datalog$sched_in_order$cargs__18477
extends AFunction {
    Object prog;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__2 = RT.var((String)"datomic.datalog", (String)"not-join-clause?");
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"free-vars");
    public static final Var const__4 = RT.var((String)"datomic.datalog", (String)"extensional?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"else");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"next");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"argvars"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public datalog$sched_in_order$cargs__18477(Object object) {
        this.prog = object;
    }

    public Object invoke(Object clause) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(clause);
        if (object2 != null && object2 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = clause;
            clause = null;
            object = iLookupThunk.get(object3);
            if (iLookupThunk == object) {
                __thunk__0__ = __site__0__.fault(object3);
                object = __thunk__0__.get(object3);
            }
        } else {
            datalog$sched_in_order$cargs__18477 this_;
            Object object4 = ((IFn)const__2.getRawRoot()).invoke(clause);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = clause;
                clause = null;
                this_ = null;
                object = ((IFn)const__3.getRawRoot()).invoke(object5);
            } else {
                Object object6 = ((IFn)const__4.getRawRoot()).invoke(this_.prog, ((IFn)const__5.getRawRoot()).invoke(clause));
                if (object6 != null && object6 != Boolean.FALSE) {
                    object = clause;
                    clause = null;
                } else {
                    Keyword keyword = const__6;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        Object object7 = clause;
                        clause = null;
                        this_ = null;
                        object = ((IFn)const__7.getRawRoot()).invoke(object7);
                    } else {
                        object = null;
                    }
                }
            }
        }
        return object;
    }
}


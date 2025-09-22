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
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$eval_clause$fn__18790;
import datomic.datalog$eval_clause$fn__18793;
import datomic.datalog$eval_clause$fn__18796;
import datomic.datalog$eval_clause$fn__18799;

public final class datalog$eval_clause
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.measure.query-stats", (String)"merge-with-clause-stats!");
    public static final Keyword const__1 = RT.keyword(null, (String)"clause");
    public static final Keyword const__2 = RT.keyword(null, (String)"rows-in");
    public static final Keyword const__4 = RT.keyword(null, (String)"binds-in");
    public static final Keyword const__5 = RT.keyword(null, (String)"binds-out");
    public static final Var const__6 = RT.var((String)"datomic.datalog", (String)"maybe-cancel");
    public static final Var const__7 = RT.var((String)"datomic.datalog", (String)"not-join-clause?");
    public static final Var const__8 = RT.var((String)"datomic.datalog", (String)"eval-not-join");
    public static final Var const__9 = RT.var((String)"datomic.datalog", (String)"project");
    public static final Keyword const__10 = RT.keyword(null, (String)"rows-out");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__15 = RT.var((String)"datomic.datalog", (String)"extensional?");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__19 = RT.keyword(null, (String)"else");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__22 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__23 = RT.keyword((String)"db.error", (String)"invalid-clause");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__26 = RT.var((String)"datomic.datalog", (String)"create-join-maps");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"empty?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"binds"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"binds"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"argvars"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"binds"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object db2, Object srcs, Object prog, Object oprog, Object clause, Object predctor, Object inrel, Object inbinds, Object next_binds, Object sched_fn, Object src, Object ans, Object ins, Object top_bounds) {
        Object object;
        ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__1, clause, const__2, RT.count((Object)inrel), const__4, inbinds, const__5, next_binds}));
        ((IFn)const__6.getRawRoot()).invoke();
        Object object2 = ((IFn)const__7.getRawRoot()).invoke(clause);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object nrel;
            Object object3 = srcs;
            srcs = null;
            Object object4 = oprog;
            oprog = null;
            Object object5 = inrel;
            inrel = null;
            Object object6 = clause;
            clause = null;
            Object object7 = nrel = ((IFn)const__8.getRawRoot()).invoke(object3, object4, object5, inbinds, object6);
            nrel = null;
            Object object8 = inbinds;
            inbinds = null;
            Object object9 = next_binds;
            next_binds = null;
            Object ret = ((IFn)const__9.getRawRoot()).invoke(object7, object8, object9);
            ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__10, RT.count((Object)ret)}));
            object = ret;
            ret = null;
        } else {
            Object object10;
            Object and__5236__auto__18813;
            Object object11;
            Object ext_QMARK_;
            Object object12;
            Object and__5236__auto__18811;
            Object object13;
            Object or__5238__auto__18810;
            Object object14;
            Object and__5236__auto__18809;
            Object object15;
            Object and__5236__auto__18808;
            Object object16 = and__5236__auto__18808 = ((IFn)const__11.getRawRoot()).invoke(clause);
            if (object16 != null && object16 != Boolean.FALSE) {
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object17 = clause;
                object15 = iLookupThunk.get(object17);
                if (iLookupThunk == object15) {
                    __thunk__0__ = __site__0__.fault(object17);
                    object15 = __thunk__0__.get(object17);
                }
            } else {
                object15 = and__5236__auto__18808;
                and__5236__auto__18808 = null;
            }
            Object exf = object15;
            Object object18 = and__5236__auto__18809 = ((IFn)const__11.getRawRoot()).invoke(clause);
            if (object18 != null && object18 != Boolean.FALSE) {
                ILookupThunk iLookupThunk = __thunk__1__;
                Object object19 = clause;
                Object object20 = iLookupThunk.get(object19);
                if (iLookupThunk == object20) {
                    __thunk__1__ = __site__1__.fault(object19);
                    object20 = __thunk__1__.get(object19);
                }
                object14 = Util.identical((Object)object20, null) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object14 = and__5236__auto__18809;
                and__5236__auto__18809 = null;
            }
            Object exp2 = object14;
            IFn iFn = (IFn)const__14.getRawRoot();
            Object object21 = or__5238__auto__18810 = exf;
            if (object21 != null && object21 != Boolean.FALSE) {
                object13 = or__5238__auto__18810;
                or__5238__auto__18810 = null;
            } else {
                object13 = exp2;
            }
            Object object22 = and__5236__auto__18811 = iFn.invoke(object13);
            if (object22 != null && object22 != Boolean.FALSE) {
                object12 = ((IFn)const__15.getRawRoot()).invoke(prog, ((IFn)const__16.getRawRoot()).invoke(clause));
            } else {
                object12 = and__5236__auto__18811;
                and__5236__auto__18811 = null;
            }
            Object object23 = ext_QMARK_ = object12;
            if (object23 != null && object23 != Boolean.FALSE) {
                object11 = clause;
            } else {
                Object object24;
                Object or__5238__auto__18812;
                Object object25 = or__5238__auto__18812 = exf;
                if (object25 != null && object25 != Boolean.FALSE) {
                    object24 = or__5238__auto__18812;
                    or__5238__auto__18812 = null;
                } else {
                    object24 = exp2;
                }
                if (object24 != null && object24 != Boolean.FALSE) {
                    IFn iFn2 = (IFn)const__17.getRawRoot();
                    ILookupThunk iLookupThunk = __thunk__2__;
                    Object object26 = clause;
                    Object object27 = iLookupThunk.get(object26);
                    if (iLookupThunk == object27) {
                        __thunk__2__ = __site__2__.fault(object26);
                        object27 = __thunk__2__.get(object26);
                    }
                    ILookupThunk iLookupThunk2 = __thunk__3__;
                    Object object28 = clause;
                    Object object29 = iLookupThunk2.get(object28);
                    if (iLookupThunk2 == object29) {
                        __thunk__3__ = __site__3__.fault(object28);
                        object29 = __thunk__3__.get(object28);
                    }
                    object11 = iFn2.invoke(object27, object29);
                } else {
                    Keyword keyword = const__19;
                    object11 = keyword != null && keyword != Boolean.FALSE ? ((IFn)const__20.getRawRoot()).invoke(clause) : null;
                }
            }
            Object args = object11;
            Object object30 = ((IFn)const__21.getRawRoot()).invoke(const__13.getRawRoot(), args);
            Object object31 = object30 != null && object30 != Boolean.FALSE ? ((IFn)const__22.getRawRoot()).invoke((Object)const__23, ((IFn)const__24.getRawRoot()).invoke((Object)"Can't have nil args in clause: ", clause)) : null;
            Object consts = ((IFn)const__25.getRawRoot()).invoke((Object)new datalog$eval_clause$fn__18790(top_bounds), args);
            Object starts = ((IFn)const__25.getRawRoot()).invoke((Object)new datalog$eval_clause$fn__18793(top_bounds), args);
            Object whiles = ((IFn)const__25.getRawRoot()).invoke((Object)new datalog$eval_clause$fn__18796(top_bounds), args);
            Object object32 = args;
            args = null;
            Object object33 = next_binds;
            next_binds = null;
            Object vec__18787 = ((IFn)const__26.getRawRoot()).invoke(inbinds, object32, object33);
            Object join = RT.nth((Object)vec__18787, (int)RT.uncheckedIntCast((long)0L), null);
            Object projx = RT.nth((Object)vec__18787, (int)RT.uncheckedIntCast((long)1L), null);
            Object object34 = vec__18787;
            vec__18787 = null;
            Object projy = RT.nth((Object)object34, (int)RT.uncheckedIntCast((long)2L), null);
            Object object35 = and__5236__auto__18813 = ((IFn)const__31.getRawRoot()).invoke(join);
            if (object35 != null && object35 != Boolean.FALSE) {
                object10 = ((IFn)const__31.getRawRoot()).invoke(inbinds);
            } else {
                object10 = and__5236__auto__18813;
                and__5236__auto__18813 = null;
            }
            Object root_QMARK_ = object10;
            Object object36 = join;
            join = null;
            Object object37 = oprog;
            oprog = null;
            Object object38 = ins;
            ins = null;
            Object object39 = starts;
            starts = null;
            Object object40 = ext_QMARK_;
            ext_QMARK_ = null;
            Object object41 = db2;
            db2 = null;
            Object object42 = top_bounds;
            top_bounds = null;
            Object object43 = inrel;
            inrel = null;
            Object object44 = sched_fn;
            sched_fn = null;
            Object object45 = clause;
            clause = null;
            Object object46 = whiles;
            whiles = null;
            Object object47 = projy;
            projy = null;
            Object object48 = exf;
            exf = null;
            Object object49 = exp2;
            exp2 = null;
            Object object50 = prog;
            prog = null;
            Object object51 = ans;
            ans = null;
            Object object52 = predctor;
            predctor = null;
            Object object53 = projx;
            projx = null;
            Object object54 = root_QMARK_;
            root_QMARK_ = null;
            Object object55 = inbinds;
            inbinds = null;
            Object object56 = src;
            src = null;
            Object object57 = consts;
            consts = null;
            Object ret = ((IFn)new datalog$eval_clause$fn__18799(object36, object37, object38, object39, object40, object41, object42, object43, object44, object45, object46, object47, object48, object49, object50, object51, object52, object53, object54, object55, object56, object57)).invoke();
            ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__10, RT.count((Object)ret)}));
            object = ret;
            ret = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14) {
        Object object15 = object;
        object = null;
        Object object16 = object2;
        object2 = null;
        Object object17 = object3;
        object3 = null;
        Object object18 = object4;
        object4 = null;
        Object object19 = object5;
        object5 = null;
        Object object20 = object6;
        object6 = null;
        Object object21 = object7;
        object7 = null;
        Object object22 = object8;
        object8 = null;
        Object object23 = object9;
        object9 = null;
        Object object24 = object10;
        object10 = null;
        Object object25 = object11;
        object11 = null;
        Object object26 = object12;
        object12 = null;
        Object object27 = object13;
        object13 = null;
        Object object28 = object14;
        object14 = null;
        return datalog$eval_clause.invokeStatic(object15, object16, object17, object18, object19, object20, object21, object22, object23, object24, object25, object26, object27, object28);
    }
}


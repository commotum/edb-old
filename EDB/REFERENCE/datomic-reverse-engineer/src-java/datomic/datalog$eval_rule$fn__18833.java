/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$eval_rule$fn__18833$fn__18837;

public final class datalog$eval_rule$fn__18833
extends AFunction {
    Object ans;
    Object db;
    Object oprog;
    Object top_bounds;
    Object inbinds;
    Object srcs;
    Object hargs;
    Object ins;
    Object head;
    Object src;
    Object multi_QMARK_;
    Object prog;
    Object inrel;
    Object cbs;
    Object sched_fn;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"meta");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"keys");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"$");
    public static final Keyword const__12 = RT.keyword(null, (String)"else");
    public static final Var const__13 = RT.var((String)"datomic.measure.query-stats", (String)"with-clause-stats");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__15 = RT.var((String)"datomic.datalog", (String)"project");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__18 = RT.var((String)"datomic.datalog", (String)"variable?");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tag"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public datalog$eval_rule$fn__18833(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12, Object object13, Object object14, Object object15) {
        this.ans = object;
        this.db = object2;
        this.oprog = object3;
        this.top_bounds = object4;
        this.inbinds = object5;
        this.srcs = object6;
        this.hargs = object7;
        this.ins = object8;
        this.head = object9;
        this.src = object10;
        this.multi_QMARK_ = object11;
        this.prog = object12;
        this.inrel = object13;
        this.cbs = object14;
        this.sched_fn = object15;
    }

    public Object invoke() {
        Object object;
        try {
            Object sbinds = this.inbinds = null;
            Object sup = this.inrel = null;
            Object cbs = this.cbs = null;
            while (true) {
                Object object2;
                Object object3;
                Object csrc;
                Object and__5236__auto__18841;
                Object object4 = cbs;
                if (object4 == null || object4 == Boolean.FALSE) break;
                Object vec__18834 = ((IFn)const__0.getRawRoot()).invoke(cbs);
                Object c = RT.nth((Object)vec__18834, (int)RT.uncheckedIntCast((long)0L), null);
                Object next_binds = RT.nth((Object)vec__18834, (int)RT.uncheckedIntCast((long)1L), null);
                Object object5 = vec__18834;
                vec__18834 = null;
                Object predctor = RT.nth((Object)object5, (int)RT.uncheckedIntCast((long)2L), null);
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object6 = ((IFn)const__6.getRawRoot()).invoke(c);
                Object object7 = iLookupThunk.get(object6);
                if (iLookupThunk == object7) {
                    __thunk__0__ = __site__0__.fault(object6);
                    object7 = __thunk__0__.get(object6);
                }
                Object object8 = and__5236__auto__18841 = (csrc = object7);
                if (object8 != null && object8 != Boolean.FALSE) {
                    object3 = this.multi_QMARK_;
                } else {
                    object3 = and__5236__auto__18841;
                    and__5236__auto__18841 = null;
                }
                if (object3 != null && object3 != Boolean.FALSE) {
                    Object x = RT.get((Object)this.db, (Object)csrc);
                    if (Util.identical((Object)x, null)) {
                        throw (Throwable)new Exception((String)((IFn)const__9.getRawRoot()).invoke((Object)"Unable to find data source: ", csrc, (Object)" in: ", ((IFn)const__10.getRawRoot()).invoke(this.db)));
                    }
                    object2 = x;
                    x = null;
                } else {
                    Object object9 = this.multi_QMARK_;
                    if (object9 != null && object9 != Boolean.FALSE) {
                        object2 = RT.get((Object)this.db, (Object)const__11);
                    } else {
                        Keyword keyword = const__12;
                        object2 = keyword != null && keyword != Boolean.FALSE ? this.db : null;
                    }
                }
                Object cdb = object2;
                Object object10 = predctor;
                predctor = null;
                Object object11 = c;
                c = null;
                Object object12 = cdb;
                cdb = null;
                Object object13 = csrc;
                csrc = null;
                Object object14 = sup;
                sup = null;
                Object object15 = sbinds;
                sbinds = null;
                Object sup1 = ((IFn)const__13.getRawRoot()).invoke((Object)new datalog$eval_rule$fn__18833$fn__18837(this.ans, object10, this.oprog, this.top_bounds, this.srcs, next_binds, this.ins, object11, this.src, object12, object13, this.prog, object14, this.sched_fn, object15));
                Object object16 = next_binds;
                next_binds = null;
                Object object17 = sup1;
                sup1 = null;
                Object object18 = cbs;
                cbs = null;
                cbs = ((IFn)const__14.getRawRoot()).invoke(object18);
                sup = object17;
                sbinds = object16;
            }
            Object object19 = sup;
            sup = null;
            Object object20 = sbinds;
            sbinds = null;
            object = ((IFn)const__15.getRawRoot()).invoke(object19, object20, ((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke(const__18.getRawRoot(), this.hargs)));
        }
        catch (Exception ex2) {
            this.head = null;
            Object ex2 = null;
            throw (Throwable)new Exception((String)((IFn)const__9.getRawRoot()).invoke((Object)"processing rule: ", this.head, (Object)", message: ", (Object)((Throwable)ex2).getMessage()), ex2);
        }
        return object;
    }
}


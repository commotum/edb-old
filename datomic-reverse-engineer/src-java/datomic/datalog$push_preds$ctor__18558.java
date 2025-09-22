/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.datalog$push_preds$ctor__18558$fn__18563;
import datomic.datalog$push_preds$ctor__18558$fn__18565;

public final class datalog$push_preds$ctor__18558
extends AFunction {
    Object srcs;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"fn");
    public static final Keyword const__4 = RT.keyword(null, (String)"argvars");
    public static final Keyword const__5 = RT.keyword(null, (String)"needs-source");
    public static final Keyword const__6 = RT.keyword(null, (String)"binds");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__11 = RT.var((String)"datomic.datalog", (String)"create-join-maps");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"meta");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)"$");
    public static final Var const__19 = RT.var((String)"datomic.measure.query-stats", (String)"acc-with-clause-stats!");
    public static final Keyword const__20 = RT.keyword(null, (String)"preds");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"tag"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public datalog$push_preds$ctor__18558(Object object) {
        this.srcs = object;
    }

    public Object invoke(Object inbinds, Object p__18557) {
        Object object;
        Object map__18559;
        Object object2;
        Object object3 = p__18557;
        p__18557 = null;
        Object map__185592 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__185592);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__185592;
            map__185592 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__185592;
            map__185592 = null;
        }
        Object clause = map__18559 = object2;
        Object f = RT.get((Object)map__18559, (Object)const__3);
        Object argvars = RT.get((Object)map__18559, (Object)const__4);
        Object needs_source = RT.get((Object)map__18559, (Object)const__5);
        Object object6 = map__18559;
        map__18559 = null;
        Object binds = RT.get((Object)object6, (Object)const__6);
        Object object7 = argvars;
        argvars = null;
        Object object8 = binds;
        binds = null;
        Object params = ((IFn)const__7.getRawRoot()).invoke(object7, object8);
        int arity = RT.count((Object)params);
        Object consts = ((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)new datalog$push_preds$ctor__18558$fn__18563(), params));
        Object object9 = params;
        params = null;
        Object object10 = inbinds;
        inbinds = null;
        Object vec__18560 = ((IFn)const__11.getRawRoot()).invoke(object9, object10, (Object)PersistentVector.EMPTY);
        Object join_map = RT.nth((Object)vec__18560, (int)RT.uncheckedIntCast((long)0L), null);
        RT.nth((Object)vec__18560, (int)RT.uncheckedIntCast((long)1L), null);
        Object object11 = vec__18560;
        vec__18560 = null;
        RT.nth((Object)object11, (int)RT.uncheckedIntCast((long)2L), null);
        Object object12 = needs_source;
        if (object12 != null && object12 != Boolean.FALSE) {
            Object object13;
            Object or__5238__auto__18571;
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object14 = ((IFn)const__17.getRawRoot()).invoke(clause);
            Object object15 = iLookupThunk.get(object14);
            if (iLookupThunk == object15) {
                __thunk__0__ = __site__0__.fault(object14);
                object15 = __thunk__0__.get(object14);
            }
            Object object16 = or__5238__auto__18571 = object15;
            if (object16 != null && object16 != Boolean.FALSE) {
                object13 = or__5238__auto__18571;
                or__5238__auto__18571 = null;
            } else {
                object13 = const__18;
            }
            object = RT.get((Object)this.srcs, (Object)object13);
        } else {
            object = null;
        }
        Object src = object;
        Object object17 = clause;
        clause = null;
        ((IFn)const__19.getRawRoot()).invoke((Object)const__20, object17);
        Object object18 = needs_source;
        needs_source = null;
        Object object19 = f;
        f = null;
        Object object20 = consts;
        consts = null;
        Object object21 = src;
        src = null;
        Object object22 = join_map;
        join_map = null;
        return new datalog$push_preds$ctor__18558$fn__18565(object18, object19, object20, arity, object21, object22);
    }
}


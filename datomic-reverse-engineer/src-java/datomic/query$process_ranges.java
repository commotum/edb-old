/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.query$process_ranges$assoc_stronger__19342;
import datomic.query$process_ranges$const_QMARK___19321;
import datomic.query$process_ranges$fn__19335;
import datomic.query$process_ranges$fn__19346;
import datomic.query$process_ranges$norm__19325;

public final class query$process_ranges
extends AFunction {
    public static final AFn const__1 = (AFn)RT.map((Object[])new Object[]{Symbol.intern(null, (String)"<"), Symbol.intern(null, (String)">"), Symbol.intern(null, (String)"<="), Symbol.intern(null, (String)">="), Symbol.intern(null, (String)">"), Symbol.intern(null, (String)"<"), Symbol.intern(null, (String)">="), Symbol.intern(null, (String)"<="), Symbol.intern(null, (String)"="), Symbol.intern(null, (String)"=")});
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keep");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"reduce");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"in-consts"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"where"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object qmap) {
        query$process_ranges$assoc_stronger__19342 assoc_stronger;
        Object consts;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = qmap;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = consts = object2;
        consts = null;
        query$process_ranges$const_QMARK___19321 const_QMARK_ = new query$process_ranges$const_QMARK___19321(object3);
        AFn swap = const__1;
        query$process_ranges$const_QMARK___19321 query$process_ranges$const_QMARK___19321 = const_QMARK_;
        const_QMARK_ = null;
        AFn aFn = swap;
        swap = null;
        query$process_ranges$norm__19325 norm = new query$process_ranges$norm__19325((Object)query$process_ranges$const_QMARK___19321, aFn);
        IFn iFn = (IFn)const__2.getRawRoot();
        query$process_ranges$norm__19325 query$process_ranges$norm__19325 = norm;
        norm = null;
        query$process_ranges$fn__19335 query$process_ranges$fn__19335 = new query$process_ranges$fn__19335((Object)query$process_ranges$norm__19325);
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = qmap;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        Object cexprs = iFn.invoke((Object)query$process_ranges$fn__19335, object5);
        query$process_ranges$assoc_stronger__19342 query$process_ranges$assoc_stronger__19342 = assoc_stronger = new query$process_ranges$assoc_stronger__19342();
        assoc_stronger = null;
        Object object6 = qmap;
        qmap = null;
        Object object7 = cexprs;
        cexprs = null;
        return ((IFn)const__4.getRawRoot()).invoke((Object)new query$process_ranges$fn__19346((Object)query$process_ranges$assoc_stronger__19342), object6, object7);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$process_ranges.invokeStatic(object2);
    }
}


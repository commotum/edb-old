/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.qtune$qtune$fn__23473;
import datomic.qtune$qtune$pred_QMARK___23459;
import datomic.qtune$qtune$sv_QMARK___23462;
import java.util.List;
import java.util.Map;

public final class qtune$qtune
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__1 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Var const__4 = RT.var((String)"datomic.query", (String)"listq->mapq");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__8 = RT.var((String)"datomic.query", (String)"process-aggregates");
    public static final Var const__9 = RT.var((String)"datomic.query", (String)"process-in-bindings");
    public static final Var const__10 = RT.var((String)"datomic.common", (String)"split-filter");
    public static final Keyword const__11 = RT.keyword(null, (String)"where");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"prn");
    public static final Keyword const__18 = RT.keyword(null, (String)"warmup");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__28 = RT.var((String)"datomic.qtune", (String)"aug");
    public static final Var const__29 = RT.var((String)"datomic.qtune", (String)"mapq->listq");
    public static final Var const__30 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__31 = RT.keyword(null, (String)"in");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__33 = RT.keyword(null, (String)"result");
    public static final Var const__35 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__36 = RT.var((String)"datomic.api", (String)"q");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"where"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"in"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object query2, ISeq args) {
        Object query3;
        Object query4;
        Object object;
        Object object2;
        Object object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(query2);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = query2;
            query2 = null;
            object3 = ((IFn)const__1.getRawRoot()).invoke(object5);
        } else {
            object3 = query2;
            query2 = null;
        }
        Object query5 = object3;
        if (query5 instanceof List) {
            Object object6 = query5;
            query5 = null;
            object2 = ((IFn)const__4.getRawRoot()).invoke(object6);
        } else {
            object2 = query5;
            query5 = null;
        }
        Object query6 = object2;
        if (!(query6 instanceof Map)) {
            throw (Throwable)new IllegalArgumentException("query must be a readable edn string, list, or map");
        }
        Object object7 = ((IFn)const__6.getRawRoot()).invoke(query6);
        if (object7 != null && object7 != Boolean.FALSE) {
            object = query6;
            query6 = null;
        } else {
            Object object8 = query6;
            query6 = null;
            object = ((IFn)const__7.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, object8);
        }
        Object oq = query4 = object;
        Object object9 = query4;
        query4 = null;
        Object object10 = query3 = ((IFn)const__8.getRawRoot()).invoke(object9);
        query3 = null;
        Object query7 = ((IFn)const__9.getRawRoot()).invoke(object10, (Object)"$__");
        qtune$qtune$pred_QMARK___23459 pred_QMARK_ = new qtune$qtune$pred_QMARK___23459();
        qtune$qtune$sv_QMARK___23462 sv_QMARK_ = new qtune$qtune$sv_QMARK___23462();
        IFn iFn = (IFn)const__10.getRawRoot();
        qtune$qtune$pred_QMARK___23459 qtune$qtune$pred_QMARK___23459 = pred_QMARK_;
        pred_QMARK_ = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object11 = query7;
        Object object12 = iLookupThunk.get(object11);
        if (iLookupThunk == object12) {
            __thunk__0__ = __site__0__.fault(object11);
            object12 = __thunk__0__.get(object11);
        }
        Object vec__23453 = iFn.invoke((Object)qtune$qtune$pred_QMARK___23459, object12);
        Object preds = RT.nth((Object)vec__23453, (int)RT.intCast((long)0L), null);
        Object object13 = vec__23453;
        vec__23453 = null;
        Object clauses = RT.nth((Object)object13, (int)RT.intCast((long)1L), null);
        qtune$qtune$sv_QMARK___23462 qtune$qtune$sv_QMARK___23462 = sv_QMARK_;
        sv_QMARK_ = null;
        Object object14 = clauses;
        clauses = null;
        Object vec__23456 = ((IFn)const__10.getRawRoot()).invoke((Object)qtune$qtune$sv_QMARK___23462, object14);
        Object svs = RT.nth((Object)vec__23456, (int)RT.intCast((long)0L), null);
        Object object15 = vec__23456;
        vec__23456 = null;
        Object rclauses = RT.nth((Object)object15, (int)RT.intCast((long)1L), null);
        Object object16 = svs;
        svs = null;
        Object svs2 = ((IFn)const__15.getRawRoot()).invoke(object16);
        Object object17 = rclauses;
        rclauses = null;
        Object rclauses2 = ((IFn)const__16.getRawRoot()).invoke(object17);
        ((IFn)const__17.getRawRoot()).invoke((Object)const__18);
        long n__5742__auto__23478 = 5L;
        for (long _ = 0L; _ < n__5742__auto__23478; ++_) {
            long start__5856__auto__23476 = System.nanoTime();
            Object ret__5857__auto__23477 = ((IFn)new qtune$qtune$fn__23473(oq, args)).invoke();
            ((IFn)const__17.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)Numbers.minus((long)System.nanoTime(), (long)start__5856__auto__23476), (double)1000000.0), (Object)" msecs"));
        }
        ((IFn)const__17.getRawRoot()).invoke();
        Object object18 = svs2;
        svs2 = null;
        Object object19 = rclauses2;
        rclauses2 = null;
        Object cs = ((IFn)const__28.getRawRoot()).invoke(query7, preds, object18, object19, (Object)args);
        IFn iFn2 = (IFn)const__29.getRawRoot();
        IFn iFn3 = (IFn)const__30.getRawRoot();
        Object object20 = oq;
        oq = null;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object21 = query7;
        query7 = null;
        Object object22 = iLookupThunk2.get(object21);
        if (iLookupThunk2 == object22) {
            __thunk__1__ = __site__1__.fault(object21);
            object22 = __thunk__1__.get(object21);
        }
        Object object23 = cs;
        cs = null;
        Object object24 = preds;
        preds = null;
        Object retq = iFn2.invoke(iFn3.invoke(object20, (Object)const__31, object22, (Object)const__11, ((IFn)const__32.getRawRoot()).invoke(object23, object24)));
        ((IFn)const__17.getRawRoot()).invoke((Object)const__33);
        long n__5742__auto__23481 = 10L;
        for (long _ = 0L; _ < n__5742__auto__23481; ++_) {
            long start__5856__auto__23479 = System.nanoTime();
            Object ret__5857__auto__23480 = ((IFn)const__35.getRawRoot()).invoke(const__36.getRawRoot(), retq, (Object)args);
            ((IFn)const__17.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)"Elapsed time: ", (Object)Numbers.divide((double)Numbers.minus((long)System.nanoTime(), (long)start__5856__auto__23479), (double)1000000.0), (Object)" msecs"));
        }
        Object object25 = retq;
        retq = null;
        return object25;
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return qtune$qtune.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}


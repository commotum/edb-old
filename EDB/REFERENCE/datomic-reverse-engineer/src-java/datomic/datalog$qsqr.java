/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$qsqr$f__18878;
import datomic.datalog$qsqr$fn__18880;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class datalog$qsqr
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"qsqr");
    public static final Var const__1 = RT.var((String)"datomic.datalog", (String)"sched-in-order");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"in");
    public static final Keyword const__6 = RT.keyword(null, (String)"find");
    public static final Keyword const__7 = RT.keyword(null, (String)"where");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"cons");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"%");
    public static final Var const__13 = RT.var((String)"datomic.datalog", (String)"rule-cache");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__17 = RT.var((String)"datomic.datalog", (String)"recursive?");
    public static final Var const__18 = RT.var((String)"datomic.datalog", (String)"bound-consts");
    public static final Var const__19 = RT.var((String)"datomic.datalog", (String)"ranges");
    public static final Keyword const__23 = RT.keyword(null, (String)"consts");
    public static final Keyword const__24 = RT.keyword(null, (String)"starts");
    public static final Keyword const__25 = RT.keyword(null, (String)"whiles");
    public static final Var const__26 = RT.var((String)"datomic.datalog", (String)"adorned-pred");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__28 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__29 = RT.keyword(null, (String)"timeout");
    public static final Var const__30 = RT.var((String)"datomic.datalog", (String)"cancel-service");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__33 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__34 = RT.var((String)"datomic.datalog", (String)"*cancel*");
    public static final Var const__35 = RT.var((String)"datomic.measure.query-stats", (String)"with-phase-stats");
    public static final Var const__36 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__37 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__38 = RT.var((String)"clojure.core", (String)"count");
    public static final Var const__39 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__40 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__43 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"arules"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"timeout"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object db2, Object query2, Object sched_fn) {
        Object object;
        Object temp__5457__auto__18883;
        Object object2;
        Object prog;
        Object oprog;
        Object object3;
        Object object4;
        Object map__18874 = query2;
        Object object5 = ((IFn)const__2.getRawRoot()).invoke(map__18874);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__18874;
            map__18874 = null;
            object4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object6)));
        } else {
            object4 = map__18874;
            map__18874 = null;
        }
        Object map__188742 = object4;
        Object from = RT.get((Object)map__188742, (Object)const__5);
        Object pargs = RT.get((Object)map__188742, (Object)const__6);
        Object object7 = map__188742;
        map__188742 = null;
        Object clauses = RT.get((Object)object7, (Object)const__7);
        Object object8 = from;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = db2;
            db2 = null;
            object3 = ((IFn)const__8.getRawRoot()).invoke(from, object9);
        } else {
            Object object10 = db2;
            db2 = null;
            object3 = ((IFn)const__9.getRawRoot()).invoke(object10);
        }
        Object db3 = object3;
        HashSet inrel = new HashSet();
        Object pred2 = ((IFn)const__10.getRawRoot()).invoke((Object)"q__");
        Object object11 = pargs;
        pargs = null;
        Object q2 = ((IFn)const__11.getRawRoot()).invoke(pred2, object11);
        Object object12 = from;
        from = null;
        Object object13 = oprog = object12 != null && object12 != Boolean.FALSE ? RT.get((Object)db3, (Object)const__12) : null;
        Object prog2 = object13 != null && object13 != Boolean.FALSE ? RT.get((Object)const__13.getRawRoot(), (Object)oprog) : null;
        IFn iFn = (IFn)const__14.getRawRoot();
        Object object14 = prog2;
        prog2 = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object15 = query2;
        Object object16 = iLookupThunk.get(object15);
        if (iLookupThunk == object16) {
            __thunk__0__ = __site__0__.fault(object15);
            object16 = __thunk__0__.get(object15);
        }
        Object object17 = prog = iFn.invoke(object14, object16);
        prog = null;
        Object object18 = clauses;
        clauses = null;
        Object prog3 = ((IFn)const__16.getRawRoot()).invoke(object17, pred2, (Object)Tuple.create((Object)((IFn)const__11.getRawRoot()).invoke(q2, object18)));
        Object rec = ((IFn)const__17.getRawRoot()).invoke(prog3, pred2);
        Object in_consts = ((IFn)const__18.getRawRoot()).invoke(db3, query2);
        Object vec__18875 = ((IFn)const__19.getRawRoot()).invoke(in_consts, query2);
        Object range_starts = RT.nth((Object)vec__18875, (int)RT.uncheckedIntCast((long)0L), null);
        Object object19 = vec__18875;
        vec__18875 = null;
        Object range_whiles = RT.nth((Object)object19, (int)RT.uncheckedIntCast((long)1L), null);
        Object[] objectArray = new Object[6];
        objectArray[0] = const__23;
        Object object20 = in_consts;
        in_consts = null;
        objectArray[1] = object20;
        objectArray[2] = const__24;
        Object object21 = range_starts;
        range_starts = null;
        objectArray[3] = object21;
        objectArray[4] = const__25;
        Object object22 = range_whiles;
        range_whiles = null;
        objectArray[5] = object22;
        IPersistentMap top_bounds = RT.mapUniqueKeys((Object[])objectArray);
        HashMap ans = new HashMap();
        Object object23 = q2;
        q2 = null;
        Object apred = ((IFn)const__26.getRawRoot()).invoke(object23);
        Object object24 = pred2;
        pred2 = null;
        IPersistentVector aresk = Tuple.create(null, (Object)object24);
        Object cancel2 = ((IFn)const__27.getRawRoot()).invoke(null);
        Object object25 = ((IFn)const__28.getRawRoot()).invoke(query2, (Object)const__29);
        if (object25 != null && object25 != Boolean.FALSE) {
            IFn iFn2 = (IFn)const__9.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object26 = query2;
            query2 = null;
            Object object27 = iLookupThunk2.get(object26);
            if (iLookupThunk2 == object27) {
                __thunk__1__ = __site__1__.fault(object26);
                object27 = __thunk__1__.get(object26);
            }
            object2 = iFn2.invoke(object27);
        } else {
            object2 = null;
        }
        Object object28 = temp__5457__auto__18883 = object2;
        if (object28 != null && object28 != Boolean.FALSE) {
            datalog$qsqr$f__18878 f;
            Object object29 = temp__5457__auto__18883;
            temp__5457__auto__18883 = null;
            Object timeout = object29;
            datalog$qsqr$f__18878 datalog$qsqr$f__18878 = f = new datalog$qsqr$f__18878(cancel2);
            f = null;
            Object object30 = timeout;
            timeout = null;
            ((ScheduledExecutorService)const__30.getRawRoot()).schedule((Callable)((Object)datalog$qsqr$f__18878), RT.longCast((Object)object30), TimeUnit.MILLISECONDS);
        }
        Object object31 = cancel2;
        cancel2 = null;
        ((IFn)const__32.getRawRoot()).invoke(((IFn)const__33.getRawRoot()).invoke((Object)const__34, object31));
        try {
            Object asnap = PersistentArrayMap.EMPTY;
            long round2 = 0L;
            while (true) {
                Object object32;
                Object or__5238__auto__18884;
                ((IFn)const__35.getRawRoot()).invoke((Object)new datalog$qsqr$fn__18880(prog3, top_bounds, inrel, oprog, apred, db3, ans, sched_fn));
                Object asnap_next = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__36.getRawRoot()).invoke(ans), ((IFn)const__37.getRawRoot()).invoke(const__38.getRawRoot(), ((IFn)const__39.getRawRoot()).invoke(ans)));
                Object object33 = or__5238__auto__18884 = ((IFn)const__40.getRawRoot()).invoke(rec);
                if (object33 != null && object33 != Boolean.FALSE) {
                    object32 = or__5238__auto__18884;
                    or__5238__auto__18884 = null;
                } else {
                    PersistentArrayMap persistentArrayMap = asnap;
                    asnap = null;
                    object32 = Util.equiv((Object)asnap_next, (Object)persistentArrayMap) ? Boolean.TRUE : Boolean.FALSE;
                }
                if (object32 != null && object32 != Boolean.FALSE) break;
                Object object34 = asnap_next;
                asnap_next = null;
                ++round2;
                asnap = object34;
            }
            object = RT.get(ans, (Object)aresk, new HashSet());
        }
        finally {
            ((IFn)const__43.getRawRoot()).invoke();
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datalog$qsqr.invokeStatic(object4, object5, object6);
    }

    public static Object invokeStatic(Object db2, Object query2) {
        Object object = db2;
        db2 = null;
        Object object2 = query2;
        query2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, const__1.getRawRoot());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$qsqr.invokeStatic(object3, object4);
    }
}


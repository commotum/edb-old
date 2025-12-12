/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class slf4j$log_time
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Keyword const__1 = RT.keyword(null, (String)"event");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"name");
    public static final Keyword const__5 = RT.keyword(null, (String)"level");
    public static final Keyword const__6 = RT.keyword(null, (String)"debug");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__9 = (AFn)Symbol.intern(null, (String)"level");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__13 = RT.var((String)"datomic.slf4j", (String)"log-end-phase-only-events");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"gensym");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__21 = (AFn)Symbol.intern(null, (String)"___8980__auto__");
    public static final AFn const__22 = (AFn)Symbol.intern((String)"clojure.core", (String)"assoc");
    public static final Keyword const__23 = RT.keyword(null, (String)"phase");
    public static final Keyword const__24 = RT.keyword(null, (String)"begin");
    public static final AFn const__25 = (AFn)Symbol.intern(null, (String)"start__8981__auto__");
    public static final AFn const__26 = (AFn)Symbol.intern((String)"java.lang.System", (String)"nanoTime");
    public static final AFn const__27 = (AFn)Symbol.intern(null, (String)"result__8982__auto__");
    public static final AFn const__28 = (AFn)Symbol.intern(null, (String)"try");
    public static final Var const__29 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Keyword const__30 = RT.keyword(null, (String)"returned");
    public static final AFn const__31 = (AFn)Symbol.intern(null, (String)"do");
    public static final AFn const__32 = (AFn)Symbol.intern(null, (String)"catch");
    public static final AFn const__33 = (AFn)Symbol.intern(null, (String)"java.lang.Throwable");
    public static final AFn const__34 = (AFn)Symbol.intern(null, (String)"t__8983__auto__");
    public static final Keyword const__35 = RT.keyword(null, (String)"threw");
    public static final AFn const__36 = (AFn)Symbol.intern((String)"clojure.core", (String)"-");
    public static final AFn const__37 = (AFn)Symbol.intern((String)"java.lang.System", (String)"nanoTime");
    public static final AFn const__38 = (AFn)Symbol.intern((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__39 = RT.var((String)"datomic.slf4j", (String)"metric-expr");
    public static final AFn const__40 = (AFn)Symbol.intern((String)"clojure.core", (String)"let");
    public static final AFn const__41 = (AFn)Symbol.intern(null, (String)"endmsg__8984__auto__");
    public static final AFn const__42 = (AFn)Symbol.intern((String)"clojure.core", (String)"merge");
    public static final AFn const__43 = (AFn)Symbol.intern((String)"clojure.core", (String)"assoc");
    public static final Keyword const__44 = RT.keyword(null, (String)"msec");
    public static final Keyword const__45 = RT.keyword(null, (String)"end");
    public static final AFn const__46 = (AFn)Symbol.intern((String)"clojure.core", (String)"when");
    public static final AFn const__47 = (AFn)Symbol.intern((String)"clojure.core", (String)"class");
    public static final AFn const__48 = (AFn)Symbol.intern(null, (String)"if");
    public static final AFn const__49 = (AFn)Symbol.intern((String)"clojure.core", (String)"contains?");
    public static final AFn const__50 = (AFn)Symbol.intern(null, (String)"throw");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"event"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object _AMPERSAND_form, Object _AMPERSAND_env, Object m, ISeq body) {
        Object level;
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(m);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__1;
            Object object3 = m;
            m = null;
            objectArray[1] = object3;
            object = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            object = m;
            m = null;
        }
        Object m2 = object;
        Object object4 = level = ((IFn)const__2.getRawRoot()).invoke((Object)"datomic.slf4j", ((IFn)const__3.getRawRoot()).invoke(RT.get((Object)m2, (Object)const__5, (Object)const__6)));
        if (object4 == null || object4 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__7.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__8.getRawRoot()).invoke((Object)const__9))));
        }
        Object object5 = m2;
        m2 = null;
        Object m3 = ((IFn)const__10.getRawRoot()).invoke(object5, (Object)const__5);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object6 = m3;
        Object object7 = iLookupThunk.get(object6);
        if (iLookupThunk == object7) {
            __thunk__0__ = __site__0__.fault(object6);
            object7 = __thunk__0__.get(object6);
        }
        Object event = object7;
        Object log_begin_QMARK_ = ((IFn)const__11.getRawRoot()).invoke(((IFn)const__12.getRawRoot()).invoke(const__13.getRawRoot(), event));
        Object msym = ((IFn)const__14.getRawRoot()).invoke((Object)"m_");
        Object elapsed = ((IFn)const__14.getRawRoot()).invoke((Object)"elapsed_");
        Object msec = ((IFn)const__14.getRawRoot()).invoke((Object)"msec_");
        Object object8 = m3;
        m3 = null;
        Object object9 = log_begin_QMARK_;
        log_begin_QMARK_ = null;
        ISeq iSeq = body;
        body = null;
        Object object10 = ((IFn)const__17.getRawRoot()).invoke(elapsed);
        Object object11 = elapsed;
        elapsed = null;
        Object object12 = ((IFn)const__17.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke(const__20.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke(msym), ((IFn)const__17.getRawRoot()).invoke(object8), object9 != null && object9 != Boolean.FALSE ? ((IFn)const__19.getRawRoot()).invoke(const__20.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__21), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke(level), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__22), ((IFn)const__17.getRawRoot()).invoke(msym), ((IFn)const__17.getRawRoot()).invoke((Object)const__23), ((IFn)const__17.getRawRoot()).invoke((Object)const__24)))))))))) : PersistentVector.EMPTY, ((IFn)const__17.getRawRoot()).invoke((Object)const__25), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__26)))), ((IFn)const__17.getRawRoot()).invoke((Object)const__27), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__28), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke(const__29.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__30), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__31), (Object)iSeq))))))), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__32), ((IFn)const__17.getRawRoot()).invoke((Object)const__33), ((IFn)const__17.getRawRoot()).invoke((Object)const__34), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke(const__29.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__35), ((IFn)const__17.getRawRoot()).invoke((Object)const__34))))))))))), object10, ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__36), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__37)))), ((IFn)const__17.getRawRoot()).invoke((Object)const__25)))), ((IFn)const__17.getRawRoot()).invoke(msec), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__38), ((IFn)const__17.getRawRoot()).invoke(object11))))))));
        Object object13 = event;
        event = null;
        Object object14 = ((IFn)const__17.getRawRoot()).invoke(((IFn)const__39.getRawRoot()).invoke(object13, msec));
        Object object15 = msym;
        msym = null;
        Object object16 = msec;
        msec = null;
        Object object17 = level;
        level = null;
        return ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__18), object12, object14, ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__40), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke(const__20.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__41), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__42), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__43), ((IFn)const__17.getRawRoot()).invoke(object15), ((IFn)const__17.getRawRoot()).invoke((Object)const__44), ((IFn)const__17.getRawRoot()).invoke(object16), ((IFn)const__17.getRawRoot()).invoke((Object)const__23), ((IFn)const__17.getRawRoot()).invoke((Object)const__45)))), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__46), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__35), ((IFn)const__17.getRawRoot()).invoke((Object)const__27)))), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__19.getRawRoot()).invoke(const__29.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__35), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__47), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__35), ((IFn)const__17.getRawRoot()).invoke((Object)const__27))))))))))))))))))))), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke(object17), ((IFn)const__17.getRawRoot()).invoke((Object)const__41))))))), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__48), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__49), ((IFn)const__17.getRawRoot()).invoke((Object)const__27), ((IFn)const__17.getRawRoot()).invoke((Object)const__30)))), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__30), ((IFn)const__17.getRawRoot()).invoke((Object)const__27)))), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__50), ((IFn)const__17.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)const__35), ((IFn)const__17.getRawRoot()).invoke((Object)const__27))))))))))));
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return slf4j$log_time.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}


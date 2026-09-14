/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$accept_index$fn__13644;
import datomic.db.Db;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class db$accept_index
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"root-id");
    public static final Keyword const__4 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__5 = RT.keyword(null, (String)"index");
    public static final Keyword const__6 = RT.keyword(null, (String)"history");
    public static final Keyword const__7 = RT.keyword(null, (String)"basisT");
    public static final Keyword const__8 = RT.keyword(null, (String)"nextT");
    public static final Keyword const__9 = RT.keyword(null, (String)"rev");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__13 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"nil?"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)".indexing"), Symbol.intern(null, (String)"db")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 17}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 11}));
    public static final AFn const__18 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"db", (String)"accept-index"), RT.keyword(null, (String)"basis-t"), RT.keyword(null, (String)"basisT")});
    public static final Var const__19 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__21 = RT.keyword(null, (String)"phase");
    public static final Keyword const__22 = RT.keyword(null, (String)"begin");
    public static final Var const__24 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__25 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__26 = RT.keyword(null, (String)"AcceptIndexMsec");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__28 = RT.keyword(null, (String)"msec");
    public static final Keyword const__29 = RT.keyword(null, (String)"end");
    public static final Keyword const__30 = RT.keyword(null, (String)"threw");
    public static final Var const__31 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__32 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__33 = RT.keyword(null, (String)"returned");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object p__13639) {
        IPersistentMap iPersistentMap;
        Object object;
        Object object2;
        Object object3 = p__13639;
        p__13639 = null;
        Object map__13640 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__13640);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__13640;
            map__13640 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__13640;
            map__13640 = null;
        }
        Object map__136402 = object2;
        Object root_id2 = RT.get((Object)map__136402, (Object)const__3);
        Object mid_index = RT.get((Object)map__136402, (Object)const__4);
        Object index2 = RT.get((Object)map__136402, (Object)const__5);
        Object history2 = RT.get((Object)map__136402, (Object)const__6);
        Object basisT = RT.get((Object)map__136402, (Object)const__7);
        Object nextT = RT.get((Object)map__136402, (Object)const__8);
        Object object6 = map__136402;
        map__136402 = null;
        Object rev = RT.get((Object)object6, (Object)const__9);
        if (!Util.identical((Object)((Db)db2).indexing, null)) throw (Throwable)((Object)new AssertionError(((IFn)const__11.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__12.getRawRoot()).invoke(const__13))));
        if (!Numbers.gt((Object)rev, (Object)((Db)db2).index_rev)) {
            object = db2;
            return object;
        }
        AFn m_13641 = const__18;
        Logger logger = LoggerFactory.getLogger((String)"datomic.db");
        if (logger.isInfoEnabled()) {
            Logger logger2 = logger;
            logger = null;
            logger2.info((String)((IFn)const__19.getRawRoot()).invoke(((IFn)const__20.getRawRoot()).invoke((Object)m_13641, (Object)const__21, (Object)const__22)));
        }
        long start__8981__auto__13662 = System.nanoTime();
        Object object7 = db2;
        db2 = null;
        Object object8 = rev;
        rev = null;
        Object object9 = nextT;
        nextT = null;
        Object object10 = basisT;
        basisT = null;
        Object object11 = mid_index;
        mid_index = null;
        Object object12 = history2;
        history2 = null;
        Object object13 = root_id2;
        root_id2 = null;
        Object object14 = index2;
        index2 = null;
        Object result__8982__auto__13663 = ((IFn)new db$accept_index$fn__13644(object7, object8, object9, object10, object11, object12, object13, object14)).invoke();
        long elapsed_13642 = System.nanoTime() - start__8981__auto__13662;
        Object msec_13643 = ((IFn)const__24.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_13642));
        ((IFn)const__25.getRawRoot()).invoke((Object)const__26, msec_13643);
        IFn iFn = (IFn)const__27.getRawRoot();
        AFn aFn = m_13641;
        m_13641 = null;
        Object object15 = msec_13643;
        msec_13643 = null;
        Object object16 = ((IFn)const__20.getRawRoot()).invoke((Object)aFn, (Object)const__28, object15, (Object)const__21, (Object)const__29);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object17 = result__8982__auto__13663;
        Object object18 = iLookupThunk.get(object17);
        if (iLookupThunk == object18) {
            __thunk__0__ = __site__0__.fault(object17);
            object18 = __thunk__0__.get(object17);
        }
        if (object18 != null && object18 != Boolean.FALSE) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__30;
            IFn iFn2 = (IFn)const__31.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object19 = result__8982__auto__13663;
            Object object20 = iLookupThunk2.get(object19);
            if (iLookupThunk2 == object20) {
                __thunk__1__ = __site__1__.fault(object19);
                object20 = __thunk__1__.get(object19);
            }
            objectArray[1] = iFn2.invoke(object20);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        Object endmsg__8984__auto__13660 = iFn.invoke(object16, iPersistentMap);
        Logger logger3 = LoggerFactory.getLogger((String)"datomic.db");
        if (logger3.isInfoEnabled()) {
            Logger logger4 = logger3;
            logger3 = null;
            Object object21 = endmsg__8984__auto__13660;
            endmsg__8984__auto__13660 = null;
            logger4.info((String)((IFn)const__19.getRawRoot()).invoke(object21));
        }
        Object object22 = ((IFn)const__32.getRawRoot()).invoke(result__8982__auto__13663, (Object)const__33);
        if (object22 != null && object22 != Boolean.FALSE) {
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object23 = result__8982__auto__13663;
            result__8982__auto__13663 = null;
            object = iLookupThunk3.get(object23);
            if (iLookupThunk3 != object) {
                return object;
            }
            __thunk__2__ = __site__2__.fault(object23);
            object = __thunk__2__.get(object23);
            return object;
        }
        ILookupThunk iLookupThunk4 = __thunk__3__;
        Object object24 = result__8982__auto__13663;
        result__8982__auto__13663 = null;
        Object object25 = iLookupThunk4.get(object24);
        if (iLookupThunk4 != object25) {
            throw (Throwable)object25;
        }
        __thunk__3__ = __site__3__.fault(object24);
        object25 = __thunk__3__.get(object24);
        throw (Throwable)object25;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$accept_index.invokeStatic(object3, object4);
    }
}


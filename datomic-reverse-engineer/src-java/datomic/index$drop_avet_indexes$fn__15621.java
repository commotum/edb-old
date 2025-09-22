/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$drop_avet_indexes$fn__15621$fn__15628;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$drop_avet_indexes$fn__15621
extends AFunction {
    Object olookup;
    Object attrids;
    Object as_of_t;
    Object store;
    public static final Keyword const__3 = RT.keyword(null, (String)"event");
    public static final Keyword const__4 = RT.keyword((String)"index", (String)"drop-avets");
    public static final Keyword const__5 = RT.keyword(null, (String)"root-id");
    public static final Keyword const__6 = RT.keyword(null, (String)"next-t");
    public static final Keyword const__7 = RT.keyword(null, (String)"attrs");
    public static final Var const__9 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__11 = RT.keyword(null, (String)"phase");
    public static final Keyword const__12 = RT.keyword(null, (String)"begin");
    public static final Var const__14 = RT.var((String)"datomic.slf4j", (String)"format-as-msec");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__16 = RT.keyword(null, (String)"msec");
    public static final Keyword const__17 = RT.keyword(null, (String)"end");
    public static final Keyword const__18 = RT.keyword(null, (String)"threw");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__21 = RT.keyword(null, (String)"returned");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"conj");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"returned"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"threw"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public index$drop_avet_indexes$fn__15621(Object object, Object object2, Object object3, Object object4) {
        this.olookup = object;
        this.attrids = object2;
        this.as_of_t = object3;
        this.store = object4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object p__15620, Object root_id2) {
        Object object;
        Object object2 = p__15620;
        p__15620 = null;
        Object vec__15622 = object2;
        Object root_ids = RT.nth((Object)vec__15622, (int)RT.uncheckedIntCast((long)0L), null);
        Object object3 = vec__15622;
        vec__15622 = null;
        Object garbage2 = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        Object object4 = root_id2;
        if (object4 != null && object4 != Boolean.FALSE) {
            IPersistentMap iPersistentMap;
            IPersistentMap m_15625 = RT.mapUniqueKeys((Object[])new Object[]{const__3, const__4, const__5, root_id2, const__6, this.as_of_t, const__7, RT.count((Object)this.attrids)});
            Logger logger = LoggerFactory.getLogger((String)"datomic.index");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.info((String)((IFn)const__9.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)m_15625, (Object)const__11, (Object)const__12)));
            }
            long start__8981__auto__15636 = System.nanoTime();
            Object object5 = root_id2;
            root_id2 = null;
            Object object6 = garbage2;
            garbage2 = null;
            Object object7 = root_ids;
            root_ids = null;
            Object result__8982__auto__15637 = ((IFn)new index$drop_avet_indexes$fn__15621$fn__15628(this.olookup, this.attrids, object5, this.store, object6, object7)).invoke();
            long elapsed_15626 = System.nanoTime() - start__8981__auto__15636;
            Object msec_15627 = ((IFn)const__14.getRawRoot()).invoke((Object)Numbers.num((long)elapsed_15626));
            IFn iFn = (IFn)const__15.getRawRoot();
            IPersistentMap iPersistentMap2 = m_15625;
            m_15625 = null;
            Object object8 = msec_15627;
            msec_15627 = null;
            Object object9 = ((IFn)const__10.getRawRoot()).invoke((Object)iPersistentMap2, (Object)const__16, object8, (Object)const__11, (Object)const__17);
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object11 = result__8982__auto__15637;
            object11 = iLookupThunk.get(object11);
            if (iLookupThunk == object11) {
                __thunk__0__ = __site__0__.fault(object10);
                object11 = __thunk__0__.get(object10);
            }
            if (object11 != null && object11 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__18;
                IFn iFn2 = (IFn)const__19.getRawRoot();
                ILookupThunk iLookupThunk2 = __thunk__1__;
                Object object13 = result__8982__auto__15637;
                object13 = iLookupThunk2.get(object13);
                if (iLookupThunk2 == object13) {
                    __thunk__1__ = __site__1__.fault(object12);
                    object13 = __thunk__1__.get(object12);
                }
                objectArray[1] = iFn2.invoke(object13);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                iPersistentMap = null;
            }
            Object endmsg__8984__auto__15634 = iFn.invoke(object9, iPersistentMap);
            Logger logger3 = LoggerFactory.getLogger((String)"datomic.index");
            if (logger3.isInfoEnabled()) {
                Logger logger4 = logger3;
                logger3 = null;
                Object object14 = endmsg__8984__auto__15634;
                endmsg__8984__auto__15634 = null;
                logger4.info((String)((IFn)const__9.getRawRoot()).invoke(object14));
            }
            Object object15 = ((IFn)const__20.getRawRoot()).invoke(result__8982__auto__15637, (Object)const__21);
            if (object15 != null && object15 != Boolean.FALSE) {
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object16 = result__8982__auto__15637;
                result__8982__auto__15637 = null;
                object = iLookupThunk3.get(object16);
                if (iLookupThunk3 != object) {
                    return object;
                }
                __thunk__2__ = __site__2__.fault(object16);
                object = __thunk__2__.get(object16);
                return object;
            }
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object17 = result__8982__auto__15637;
            result__8982__auto__15637 = null;
            Object object18 = iLookupThunk4.get(object17);
            if (iLookupThunk4 != object18) {
                throw (Throwable)object18;
            }
            __thunk__3__ = __site__3__.fault(object17);
            object18 = __thunk__3__.get(object17);
            throw (Throwable)object18;
        }
        Object object19 = root_ids;
        root_ids = null;
        Object object20 = root_id2;
        root_id2 = null;
        Object object21 = garbage2;
        garbage2 = null;
        object = Tuple.create((Object)((IFn)const__22.getRawRoot()).invoke(object19, object20), (Object)object21);
        return object;
    }
}


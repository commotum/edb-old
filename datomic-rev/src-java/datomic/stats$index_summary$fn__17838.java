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
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
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
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class stats$index_summary$fn__17838
extends AFunction {
    Object partfn;
    public static final Keyword const__3 = RT.keyword(null, (String)"data-count");
    public static final Keyword const__5 = RT.keyword(null, (String)"seg-count");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"data-count"), 0L, RT.keyword(null, (String)"seg-count"), 0L});
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"assoc");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"count"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public stats$index_summary$fn__17838(Object object) {
        this.partfn = object;
    }

    public Object invoke(Object acc, Object entry) {
        Object object;
        Object temp__5455__auto__17842;
        Object object2;
        Object G__17839;
        Object object3;
        Object G__178392 = entry;
        if (Util.identical((Object)G__178392, null)) {
            object3 = null;
        } else {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object4 = G__178392;
            G__178392 = null;
            object3 = iLookupThunk.get(object4);
            if (iLookupThunk == object3) {
                __thunk__0__ = __site__0__.fault(object4);
                object3 = G__17839 = __thunk__0__.get(object4);
            }
        }
        if (Util.identical(G__17839, null)) {
            object2 = null;
        } else {
            Object object5 = G__17839;
            G__17839 = null;
            object2 = ((IFn)this_.partfn).invoke(object5);
        }
        Object object6 = temp__5455__auto__17842 = object2;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object map__17840;
            Object object7;
            Object object8 = temp__5455__auto__17842;
            temp__5455__auto__17842 = null;
            Object k = object8;
            Object map__178402 = RT.get((Object)acc, (Object)k, (Object)const__6);
            Object object9 = ((IFn)const__7.getRawRoot()).invoke(map__178402);
            if (object9 != null && object9 != Boolean.FALSE) {
                Object object10 = map__178402;
                map__178402 = null;
                object7 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__8.getRawRoot()).invoke(object10)));
            } else {
                object7 = map__178402;
                map__178402 = null;
            }
            Object m = map__17840 = object7;
            Object data_count = RT.get((Object)map__17840, (Object)const__3);
            Object object11 = map__17840;
            map__17840 = null;
            Object seg_count = RT.get((Object)object11, (Object)const__5);
            IFn iFn = (IFn)const__9.getRawRoot();
            Object object12 = m;
            m = null;
            Object object13 = seg_count;
            seg_count = null;
            Number number = Numbers.inc((Object)object13);
            Object object14 = data_count;
            data_count = null;
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object15 = entry;
            entry = null;
            Object object16 = iLookupThunk.get(object15);
            if (iLookupThunk == object16) {
                __thunk__1__ = __site__1__.fault(object15);
                object16 = __thunk__1__.get(object15);
            }
            Object m2 = iFn.invoke(object12, (Object)const__5, (Object)number, (Object)const__3, (Object)Numbers.add((Object)object14, (Object)object16));
            Object object17 = acc;
            acc = null;
            Object object18 = k;
            k = null;
            Object object19 = m2;
            m2 = null;
            stats$index_summary$fn__17838 this_ = null;
            object = ((IFn)const__9.getRawRoot()).invoke(object17, object18, object19);
        } else {
            object = acc;
            Object var1_1 = null;
        }
        return object;
    }
}


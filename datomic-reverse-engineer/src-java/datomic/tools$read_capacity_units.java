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
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;

public final class tools$read_capacity_units
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.uri", (String)"parse");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__6 = RT.keyword(null, (String)"ddb");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"resolve");
    public static final AFn const__11 = (AFn)Symbol.intern((String)"datomic.ddb", (String)"describe-table");
    public static final AFn const__12 = (AFn)Symbol.intern((String)"datomic.ddb", (String)"client");
    public static final Keyword const__13 = RT.keyword(null, (String)"region");
    public static final Keyword const__14 = RT.keyword(null, (String)"tableName");
    public static final Keyword const__16 = RT.keyword(null, (String)"ddb+s3");
    public static final AFn const__17 = (AFn)Symbol.intern((String)"datomic.ddb", (String)"describe-table");
    public static final AFn const__18 = (AFn)Symbol.intern((String)"datomic.ddb", (String)"client");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"region"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"system-root"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"table"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"provisionedThroughput"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"readCapacityUnits"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"aws-region"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"aws-dynamodb-table"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"table"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"provisionedThroughput"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"readCapacityUnits"));
    static ILookupThunk __thunk__9__ = __site__9__;

    public static Object invokeStatic(Object uri2) {
        Object object;
        Object map__21784;
        Object object2;
        Object object3 = uri2;
        uri2 = null;
        Object map__217842 = ((IFn)const__0.getRawRoot()).invoke(object3);
        Object object4 = ((IFn)const__1.getRawRoot()).invoke(map__217842);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__217842;
            map__217842 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__217842;
            map__217842 = null;
        }
        Object parsed_uri = map__21784 = object2;
        Object object6 = map__21784;
        map__21784 = null;
        Object protocol = RT.get((Object)object6, (Object)const__4);
        if (Util.equiv((Object)const__6, (Object)protocol)) {
            Object object7;
            ILookupThunk iLookupThunk = __thunk__4__;
            ILookupThunk iLookupThunk2 = __thunk__3__;
            ILookupThunk iLookupThunk3 = __thunk__2__;
            IFn iFn = (IFn)((IFn)const__10.getRawRoot()).invoke((Object)const__11);
            IFn iFn2 = (IFn)((IFn)const__10.getRawRoot()).invoke((Object)const__12);
            Object[] objectArray = new Object[2];
            objectArray[0] = const__13;
            ILookupThunk iLookupThunk4 = __thunk__0__;
            Object object8 = parsed_uri;
            Object object9 = iLookupThunk4.get(object8);
            if (iLookupThunk4 == object9) {
                __thunk__0__ = __site__0__.fault(object8);
                object9 = __thunk__0__.get(object8);
            }
            objectArray[1] = object9;
            Object object10 = iFn2.invoke(null, (Object)RT.mapUniqueKeys((Object[])objectArray));
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__14;
            ILookupThunk iLookupThunk5 = __thunk__1__;
            Object object11 = parsed_uri;
            parsed_uri = null;
            Object object12 = iLookupThunk5.get(object11);
            if (iLookupThunk5 == object12) {
                __thunk__1__ = __site__1__.fault(object11);
                object12 = __thunk__1__.get(object11);
            }
            objectArray2[1] = object12;
            Object object13 = iFn.invoke(object10, (Object)RT.mapUniqueKeys((Object[])objectArray2));
            Object object14 = iLookupThunk3.get(object13);
            if (iLookupThunk3 == object14) {
                __thunk__2__ = __site__2__.fault(object13);
                object14 = __thunk__2__.get(object13);
            }
            if (iLookupThunk2 == (object7 = iLookupThunk2.get(object14))) {
                __thunk__3__ = __site__3__.fault(object14);
                object7 = __thunk__3__.get(object14);
            }
            if (iLookupThunk == (object = iLookupThunk.get(object7))) {
                __thunk__4__ = __site__4__.fault(object7);
                object = __thunk__4__.get(object7);
            }
        } else {
            Object object15 = protocol;
            protocol = null;
            if (Util.equiv((Object)const__16, (Object)object15)) {
                Object object16;
                ILookupThunk iLookupThunk = __thunk__9__;
                ILookupThunk iLookupThunk6 = __thunk__8__;
                ILookupThunk iLookupThunk7 = __thunk__7__;
                IFn iFn = (IFn)((IFn)const__10.getRawRoot()).invoke((Object)const__17);
                IFn iFn3 = (IFn)((IFn)const__10.getRawRoot()).invoke((Object)const__18);
                Object[] objectArray = new Object[2];
                objectArray[0] = const__13;
                ILookupThunk iLookupThunk8 = __thunk__5__;
                Object object17 = parsed_uri;
                Object object18 = iLookupThunk8.get(object17);
                if (iLookupThunk8 == object18) {
                    __thunk__5__ = __site__5__.fault(object17);
                    object18 = __thunk__5__.get(object17);
                }
                objectArray[1] = object18;
                Object object19 = iFn3.invoke(null, (Object)RT.mapUniqueKeys((Object[])objectArray));
                Object[] objectArray3 = new Object[2];
                objectArray3[0] = const__14;
                ILookupThunk iLookupThunk9 = __thunk__6__;
                Object object20 = parsed_uri;
                parsed_uri = null;
                Object object21 = iLookupThunk9.get(object20);
                if (iLookupThunk9 == object21) {
                    __thunk__6__ = __site__6__.fault(object20);
                    object21 = __thunk__6__.get(object20);
                }
                objectArray3[1] = object21;
                Object object22 = iFn.invoke(object19, (Object)RT.mapUniqueKeys((Object[])objectArray3));
                Object object23 = iLookupThunk7.get(object22);
                if (iLookupThunk7 == object23) {
                    __thunk__7__ = __site__7__.fault(object22);
                    object23 = __thunk__7__.get(object22);
                }
                if (iLookupThunk6 == (object16 = iLookupThunk6.get(object23))) {
                    __thunk__8__ = __site__8__.fault(object23);
                    object16 = __thunk__8__.get(object23);
                }
                if (iLookupThunk == (object = iLookupThunk.get(object16))) {
                    __thunk__9__ = __site__9__.fault(object16);
                    object = __thunk__9__.get(object16);
                }
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$read_capacity_units.invokeStatic(object2);
    }
}


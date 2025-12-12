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
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class coordination$endpoint__GT_server_spec
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"servers");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__4 = RT.keyword(null, (String)"allowed-clients");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Keyword const__9 = RT.keyword(null, (String)"max-value-bytes");
    public static final Object const__10 = 1000000L;
    public static final Keyword const__11 = RT.keyword(null, (String)"metric-prefix");
    public static final Keyword const__12 = RT.keyword(null, (String)"Valcache");
    public static final Keyword const__13 = RT.keyword(null, (String)"repair?");
    public static final Keyword const__14 = RT.keyword(null, (String)"ttl");
    public static final Object const__15 = 0L;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"vc-port"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"host"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"vc-port"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"host"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"alt-host"));
    static ILookupThunk __thunk__4__ = __site__4__;

    public static Object invokeStatic(Object endpoint, Object repair_QMARK_) {
        IPersistentMap iPersistentMap;
        Object temp__5457__auto__11705;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = endpoint;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object object3 = temp__5457__auto__11705 = object2;
        if (object3 != null && object3 != Boolean.FALSE) {
            temp__5457__auto__11705 = null;
            Object[] objectArray = new Object[12];
            objectArray[0] = const__1;
            IFn iFn = (IFn)const__2.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object4 = endpoint;
            Object object5 = iLookupThunk2.get(object4);
            if (iLookupThunk2 == object5) {
                __thunk__1__ = __site__1__.fault(object4);
                object5 = __thunk__1__.get(object4);
            }
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object6 = endpoint;
            Object object7 = iLookupThunk3.get(object6);
            if (iLookupThunk3 == object7) {
                __thunk__2__ = __site__2__.fault(object6);
                object7 = __thunk__2__.get(object6);
            }
            objectArray[1] = iFn.invoke(object5, (Object)":", object7);
            objectArray[2] = const__4;
            IFn iFn2 = (IFn)const__5.getRawRoot();
            Object object8 = ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot());
            ILookupThunk iLookupThunk4 = __thunk__3__;
            Object object9 = endpoint;
            Object object10 = iLookupThunk4.get(object9);
            if (iLookupThunk4 == object10) {
                __thunk__3__ = __site__3__.fault(object9);
                object10 = __thunk__3__.get(object9);
            }
            ILookupThunk iLookupThunk5 = __thunk__4__;
            Object object11 = endpoint;
            endpoint = null;
            Object object12 = iLookupThunk5.get(object11);
            if (iLookupThunk5 == object12) {
                __thunk__4__ = __site__4__.fault(object11);
                object12 = __thunk__4__.get(object11);
            }
            objectArray[3] = iFn2.invoke((Object)PersistentHashSet.EMPTY, object8, (Object)Tuple.create((Object)object10, (Object)object12));
            objectArray[4] = const__9;
            objectArray[5] = const__10;
            objectArray[6] = const__11;
            objectArray[7] = const__12;
            objectArray[8] = const__13;
            Object object13 = repair_QMARK_;
            repair_QMARK_ = null;
            objectArray[9] = object13;
            objectArray[10] = const__14;
            objectArray[11] = const__15;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        } else {
            iPersistentMap = null;
        }
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return coordination$endpoint__GT_server_spec.invokeStatic(object3, object4);
    }
}


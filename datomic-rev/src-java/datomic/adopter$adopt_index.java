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
 *  clojure.lang.Var
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
import clojure.lang.Var;

public final class adopter$adopt_index
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"memlog-txes-since");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"accept-txes");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"compare-and-set!");
    public static final Keyword const__7 = RT.keyword(null, (String)"basis-db");
    public static final Keyword const__8 = RT.keyword(null, (String)"adopted-db");
    public static final Keyword const__9 = RT.keyword(null, (String)"msec");
    public static final Keyword const__11 = RT.keyword(null, (String)"iterations");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"basisT"));
    static ILookupThunk __thunk__3__ = __site__3__;

    public static Object invokeStatic(Object db_ref, Object adopt_db) {
        IPersistentMap iPersistentMap;
        Object initial_db = ((IFn)const__0.getRawRoot()).invoke(db_ref);
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = adopt_db;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = initial_db;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        if (Numbers.lt((Object)object2, (Object)object4)) {
            iPersistentMap = null;
        } else {
            long msec = System.currentTimeMillis();
            IFn iFn = (IFn)const__3.getRawRoot();
            ILookupThunk iLookupThunk3 = __thunk__2__;
            Object object5 = adopt_db;
            Object object6 = iLookupThunk3.get(object5);
            if (iLookupThunk3 == object6) {
                __thunk__2__ = __site__2__.fault(object5);
                object6 = __thunk__2__.get(object5);
            }
            Object txes = iFn.invoke(initial_db, object6);
            Object object7 = initial_db;
            initial_db = null;
            Object db2 = object7;
            Object object8 = adopt_db;
            adopt_db = null;
            Object object9 = txes;
            txes = null;
            Object adopt_db2 = ((IFn)const__4.getRawRoot()).invoke(object8, object9);
            long n = 1L;
            while (true) {
                Object object10 = ((IFn)const__6.getRawRoot()).invoke(db_ref, db2, adopt_db2);
                if (object10 != null && object10 != Boolean.FALSE) {
                    Object[] objectArray = new Object[8];
                    objectArray[0] = const__7;
                    Object object11 = db2;
                    db2 = null;
                    objectArray[1] = object11;
                    objectArray[2] = const__8;
                    Object object12 = adopt_db2;
                    adopt_db2 = null;
                    objectArray[3] = object12;
                    objectArray[4] = const__9;
                    objectArray[5] = Numbers.num((long)Numbers.minus((long)System.currentTimeMillis(), (long)msec));
                    objectArray[6] = const__11;
                    objectArray[7] = Numbers.num((long)n);
                    iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
                    break;
                }
                Object db3 = ((IFn)const__0.getRawRoot()).invoke(db_ref);
                IFn iFn2 = (IFn)const__3.getRawRoot();
                ILookupThunk iLookupThunk4 = __thunk__3__;
                Object object13 = adopt_db2;
                Object object14 = iLookupThunk4.get(object13);
                if (iLookupThunk4 == object14) {
                    __thunk__3__ = __site__3__.fault(object13);
                    object14 = __thunk__3__.get(object13);
                }
                Object txes2 = iFn2.invoke(db3, object14);
                Object object15 = db3;
                db3 = null;
                Object object16 = adopt_db2;
                adopt_db2 = null;
                Object object17 = txes2;
                txes2 = null;
                n = Numbers.inc((long)n);
                adopt_db2 = ((IFn)const__4.getRawRoot()).invoke(object16, object17);
                db2 = object15;
            }
        }
        return iPersistentMap;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return adopter$adopt_index.invokeStatic(object3, object4);
    }
}


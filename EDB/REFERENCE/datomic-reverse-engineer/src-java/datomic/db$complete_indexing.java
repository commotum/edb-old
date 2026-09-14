/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
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
import datomic.db.Db;

public final class db$complete_indexing
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"root-id");
    public static final Keyword const__4 = RT.keyword(null, (String)"index");
    public static final Keyword const__5 = RT.keyword(null, (String)"mid-index");
    public static final Keyword const__6 = RT.keyword(null, (String)"history");
    public static final Keyword const__7 = RT.keyword(null, (String)"basisT");
    public static final Keyword const__8 = RT.keyword(null, (String)"nextT");
    public static final Keyword const__9 = RT.keyword(null, (String)"rev");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"recalc-elements");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__14 = RT.keyword(null, (String)"indexing");
    public static final Keyword const__15 = RT.keyword(null, (String)"indexingNextT");
    public static final Keyword const__16 = RT.keyword(null, (String)"memlog");
    public static final Var const__17 = RT.var((String)"datomic.db", (String)"trim-log");
    public static final Keyword const__18 = RT.keyword(null, (String)"indexBasisT");
    public static final Keyword const__19 = RT.keyword(null, (String)"index-root-id");
    public static final Keyword const__20 = RT.keyword(null, (String)"index-rev");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__22 = RT.keyword(null, (String)"local-index-basis");
    public static final Keyword const__23 = RT.keyword(null, (String)"stored-index-basis");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"indexBasisT"));
    static ILookupThunk __thunk__0__ = __site__0__;

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static Object invokeStatic(Object db2, Object p__13634) {
        Object object;
        Object object2;
        Object and__5236__auto__13637;
        Object object3;
        Object object4 = p__13634;
        p__13634 = null;
        Object map__13635 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__13635);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__13635;
            map__13635 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__13635;
            map__13635 = null;
        }
        Object map__136352 = object3;
        Object root_id2 = RT.get((Object)map__136352, (Object)const__3);
        Object index2 = RT.get((Object)map__136352, (Object)const__4);
        Object mid_index = RT.get((Object)map__136352, (Object)const__5);
        Object history2 = RT.get((Object)map__136352, (Object)const__6);
        Object basisT = RT.get((Object)map__136352, (Object)const__7);
        Object nextT = RT.get((Object)map__136352, (Object)const__8);
        Object object7 = map__136352;
        map__136352 = null;
        Object rev = RT.get((Object)object7, (Object)const__9);
        Object object8 = and__5236__auto__13637 = ((Db)db2).indexing;
        if (object8 != null && object8 != Boolean.FALSE) {
            object2 = Numbers.gt((Object)rev, (Object)((Db)db2).index_rev) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object2 = and__5236__auto__13637;
            and__5236__auto__13637 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object9 = nextT;
            nextT = null;
            if (!Util.equiv((Object)((Db)db2).indexingNextT, (Object)object9)) {
                IFn iFn = (IFn)const__21.getRawRoot();
                Object[] objectArray = new Object[4];
                objectArray[0] = const__22;
                ILookupThunk iLookupThunk = __thunk__0__;
                Object object10 = db2;
                db2 = null;
                Object object11 = iLookupThunk.get(object10);
                if (iLookupThunk == object11) {
                    __thunk__0__ = __site__0__.fault(object10);
                    object11 = __thunk__0__.get(object10);
                }
                objectArray[1] = object11;
                objectArray[2] = const__23;
                Object object12 = basisT;
                basisT = null;
                objectArray[3] = object12;
                throw (Throwable)iFn.invoke((Object)"Indexing surpassed by another indexing process", (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
        } else {
            object = db2;
            return object;
        }
        Object object13 = db2;
        Object object14 = db2;
        db2 = null;
        Object object15 = index2;
        index2 = null;
        Object object16 = mid_index;
        mid_index = null;
        Object object17 = history2;
        history2 = null;
        Object object18 = root_id2;
        root_id2 = null;
        Object object19 = rev;
        rev = null;
        Object object20 = ((IFn)const__13.getRawRoot()).invoke(object13, (Object)const__14, null, (Object)const__15, null, (Object)const__16, ((IFn)const__17.getRawRoot()).invoke(((Db)object14).memlog, basisT), (Object)const__4, object15, (Object)const__5, object16, (Object)const__18, basisT, (Object)const__6, object17, (Object)const__19, object18, (Object)const__20, object19);
        Object object21 = basisT;
        basisT = null;
        object = ((IFn)const__12.getRawRoot()).invoke(object20, object21);
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$complete_indexing.invokeStatic(object3, object4);
    }
}


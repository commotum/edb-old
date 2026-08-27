/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.IFn$OL
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;

public final class integrity$aevt_dquark_seq$fn__22326$fn__22331
extends AFunction {
    Object index;
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Keyword const__6 = RT.keyword(null, (String)"tx");
    public static final Keyword const__7 = RT.keyword(null, (String)"added");
    public static final Keyword const__8 = RT.keyword(null, (String)"index");
    public static final Keyword const__9 = RT.keyword(null, (String)"datom");
    public static final Keyword const__10 = RT.keyword(null, (String)"attrid");
    public static final Keyword const__11 = RT.keyword(null, (String)"part");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"resolve-kw");
    public static final Var const__13 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Keyword const__14 = RT.keyword(null, (String)"eidx");
    public static final Var const__15 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Keyword const__16 = RT.keyword(null, (String)"t");
    public static final Var const__17 = RT.var((String)"datomic.api", (String)"tx->t");

    public integrity$aevt_dquark_seq$fn__22326$fn__22331(Object object, Object object2) {
        this.index = object;
        this.db = object2;
    }

    public Object invoke(Object p__22330) {
        Object map__22332;
        Object object;
        Object object2 = p__22330;
        p__22330 = null;
        Object map__223322 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__223322);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__223322;
            map__223322 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__223322;
            map__223322 = null;
        }
        Object datom = map__22332 = object;
        Object e = RT.get((Object)map__22332, (Object)const__3);
        Object a = RT.get((Object)map__22332, (Object)const__4);
        Object v = RT.get((Object)map__22332, (Object)const__5);
        Object tx = RT.get((Object)map__22332, (Object)const__6);
        Object object5 = map__22332;
        map__22332 = null;
        Object added = RT.get((Object)object5, (Object)const__7);
        Object[] objectArray = new Object[18];
        objectArray[0] = const__5;
        Object object6 = v;
        v = null;
        objectArray[1] = object6;
        objectArray[2] = const__8;
        objectArray[3] = this.index;
        objectArray[4] = const__9;
        Object object7 = datom;
        datom = null;
        objectArray[5] = object7;
        objectArray[6] = const__10;
        objectArray[7] = a;
        objectArray[8] = const__7;
        Object object8 = added;
        added = null;
        objectArray[9] = object8;
        objectArray[10] = const__11;
        objectArray[11] = ((IFn)const__12.getRawRoot()).invoke(this.db, (Object)Numbers.num((long)((IFn.LL)const__13.getRawRoot()).invokePrim(RT.longCast((Object)((Number)e)))));
        objectArray[12] = const__14;
        Object object9 = e;
        e = null;
        objectArray[13] = Numbers.num((long)((IFn.LL)const__15.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object9))));
        objectArray[14] = const__16;
        Object object10 = tx;
        tx = null;
        objectArray[15] = Numbers.num((long)((IFn.OL)const__17.getRawRoot()).invokePrim(object10));
        objectArray[16] = const__4;
        Object object11 = a;
        a = null;
        objectArray[17] = ((IFn)const__12.getRawRoot()).invoke(this.db, object11);
        return RT.mapUniqueKeys((Object[])objectArray);
    }
}


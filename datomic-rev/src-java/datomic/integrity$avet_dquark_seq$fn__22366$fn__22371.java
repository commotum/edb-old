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

public final class integrity$avet_dquark_seq$fn__22366$fn__22371
extends AFunction {
    Object db;
    Object index;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Keyword const__6 = RT.keyword(null, (String)"tx");
    public static final Keyword const__7 = RT.keyword(null, (String)"added");
    public static final Keyword const__8 = RT.keyword(null, (String)"datom");
    public static final Keyword const__9 = RT.keyword(null, (String)"index");
    public static final Keyword const__10 = RT.keyword(null, (String)"part");
    public static final Var const__11 = RT.var((String)"datomic.db", (String)"resolve-kw");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Keyword const__13 = RT.keyword(null, (String)"eidx");
    public static final Var const__14 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Keyword const__15 = RT.keyword(null, (String)"t");
    public static final Var const__16 = RT.var((String)"datomic.api", (String)"tx->t");

    public integrity$avet_dquark_seq$fn__22366$fn__22371(Object object, Object object2) {
        this.db = object;
        this.index = object2;
    }

    public Object invoke(Object p__22370) {
        Object map__22372;
        Object object;
        Object object2 = p__22370;
        p__22370 = null;
        Object map__223722 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__223722);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__223722;
            map__223722 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__223722;
            map__223722 = null;
        }
        Object datom = map__22372 = object;
        Object e = RT.get((Object)map__22372, (Object)const__3);
        Object a = RT.get((Object)map__22372, (Object)const__4);
        Object v = RT.get((Object)map__22372, (Object)const__5);
        Object tx = RT.get((Object)map__22372, (Object)const__6);
        Object object5 = map__22372;
        map__22372 = null;
        Object added = RT.get((Object)object5, (Object)const__7);
        Object[] objectArray = new Object[16];
        objectArray[0] = const__8;
        Object object6 = datom;
        datom = null;
        objectArray[1] = object6;
        objectArray[2] = const__9;
        objectArray[3] = this.index;
        objectArray[4] = const__10;
        objectArray[5] = ((IFn)const__11.getRawRoot()).invoke(this.db, (Object)Numbers.num((long)((IFn.LL)const__12.getRawRoot()).invokePrim(RT.longCast((Object)((Number)e)))));
        objectArray[6] = const__13;
        Object object7 = e;
        e = null;
        objectArray[7] = Numbers.num((long)((IFn.LL)const__14.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object7))));
        objectArray[8] = const__4;
        Object object8 = a;
        a = null;
        objectArray[9] = ((IFn)const__11.getRawRoot()).invoke(this.db, object8);
        objectArray[10] = const__5;
        Object object9 = v;
        v = null;
        objectArray[11] = object9;
        objectArray[12] = const__15;
        Object object10 = tx;
        tx = null;
        objectArray[13] = Numbers.num((long)((IFn.OL)const__16.getRawRoot()).invokePrim(object10));
        objectArray[14] = const__7;
        Object object11 = added;
        added = null;
        objectArray[15] = object11;
        return RT.mapUniqueKeys((Object[])objectArray);
    }
}


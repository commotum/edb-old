/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.backup$verify_backup$fn__20359;
import datomic.backup$verify_backup$progress__20357;

public final class backup$verify_backup
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"backup-uri");
    public static final Keyword const__4 = RT.keyword(null, (String)"t");
    public static final Keyword const__5 = RT.keyword(null, (String)"read-all");
    public static final Var const__6 = RT.var((String)"datomic.backup", (String)"create-storage");
    public static final Var const__7 = RT.var((String)"datomic.backup", (String)"read-roots");
    public static final Var const__8 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__9 = RT.keyword((String)"verify", (String)"roots-missing");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__11 = RT.keyword((String)"backup", (String)"version");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__15 = RT.var((String)"datomic.backup", (String)"backup-seg-ids");
    public static final Var const__16 = RT.var((String)"datomic.backup", (String)"missing-seg-ids");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"merge");
    public static final Keyword const__19 = RT.keyword(null, (String)"total-segments");
    public static final Keyword const__20 = RT.keyword(null, (String)"missing-segments");
    public static final Keyword const__21 = RT.keyword(null, (String)"unreadable-segments");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"doall");
    public static final Var const__23 = RT.var((String)"datomic.backup", (String)"unreadable-seg-ids");
    public static final Var const__25 = RT.var((String)"datomic.backup", (String)"create-restore-job");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"map-indexed");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"lookup"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object p__20354) {
        IPersistentMap iPersistentMap;
        Object version2;
        Object map__20356;
        Object object;
        Object object2;
        Object or__5238__auto__20362;
        Object object3;
        Object object4 = p__20354;
        p__20354 = null;
        Object map__20355 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__20355);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__20355;
            map__20355 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__20355;
            map__20355 = null;
        }
        Object map__203552 = object3;
        Object backup_uri = RT.get((Object)map__203552, (Object)const__3);
        Object t = RT.get((Object)map__203552, (Object)const__4);
        Object object7 = map__203552;
        map__203552 = null;
        Object read_all2 = RT.get((Object)object7, (Object)const__5);
        Object object8 = backup_uri;
        backup_uri = null;
        Object backup_storage = ((IFn)const__6.getRawRoot()).invoke(object8, null);
        Object object9 = or__5238__auto__20362 = ((IFn)const__7.getRawRoot()).invoke(t, backup_storage);
        if (object9 != null && object9 != Boolean.FALSE) {
            object2 = or__5238__auto__20362;
            or__5238__auto__20362 = null;
        } else {
            object2 = ((IFn)const__8.getRawRoot()).invoke((Object)const__9, ((IFn)const__10.getRawRoot()).invoke((Object)"No database root for t ", t));
        }
        Object map__203562 = object2;
        Object object10 = ((IFn)const__0.getRawRoot()).invoke(map__203562);
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = map__203562;
            map__203562 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object11)));
        } else {
            object = map__203562;
            map__203562 = null;
        }
        Object object12 = map__20356 = object;
        map__20356 = null;
        Object object13 = version2 = RT.get((Object)object12, (Object)const__11);
        version2 = null;
        if (Numbers.lt((Object)object13, (long)3L)) {
            throw (Throwable)new RuntimeException("Verify not supported for pre-2015 backup format.");
        }
        Object seg_id_set = ((IFn)const__14.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__15.getRawRoot()).invoke(backup_storage, t));
        Object missing = ((IFn)const__16.getRawRoot()).invoke(backup_storage, seg_id_set);
        int segcount = RT.count((Object)seg_id_set);
        backup$verify_backup$progress__20357 progress = new backup$verify_backup$progress__20357(segcount);
        IFn iFn = (IFn)const__18.getRawRoot();
        Object[] objectArray = new Object[6];
        objectArray[0] = const__4;
        objectArray[1] = t;
        objectArray[2] = const__19;
        objectArray[3] = segcount;
        objectArray[4] = const__20;
        Object object14 = missing;
        missing = null;
        objectArray[5] = object14;
        IPersistentMap iPersistentMap2 = RT.mapUniqueKeys((Object[])objectArray);
        Object object15 = read_all2;
        read_all2 = null;
        if (object15 != null && object15 != Boolean.FALSE) {
            Object[] objectArray2 = new Object[2];
            objectArray2[0] = const__21;
            IFn iFn2 = (IFn)const__22.getRawRoot();
            IFn iFn3 = (IFn)const__23.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object16 = backup_storage;
            backup_storage = null;
            Object object17 = t;
            t = null;
            Object object18 = ((IFn)const__25.getRawRoot()).invoke(object16, object17);
            Object object19 = iLookupThunk.get(object18);
            if (iLookupThunk == object19) {
                __thunk__0__ = __site__0__.fault(object18);
                object19 = __thunk__0__.get(object18);
            }
            backup$verify_backup$progress__20357 backup$verify_backup$progress__20357 = progress;
            progress = null;
            Object object20 = seg_id_set;
            seg_id_set = null;
            objectArray2[1] = iFn2.invoke(iFn3.invoke(object19, ((IFn)const__26.getRawRoot()).invoke((Object)new backup$verify_backup$fn__20359((Object)backup$verify_backup$progress__20357), object20)));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray2);
        } else {
            iPersistentMap = null;
        }
        return iFn.invoke((Object)iPersistentMap2, iPersistentMap);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return backup$verify_backup.invokeStatic(object2);
    }
}


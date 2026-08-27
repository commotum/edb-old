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
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.backup$create_restore_job$fn__20238;

public final class backup$create_restore_job
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.backup", (String)"read-roots");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__2 = RT.keyword((String)"restore", (String)"roots-missing");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"datomic.backup", (String)"substorage");
    public static final Var const__5 = RT.var((String)"datomic.backup", (String)"storage-olookup");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__10 = RT.keyword(null, (String)"index-root-id");
    public static final Keyword const__11 = RT.keyword(null, (String)"log-root-id");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__13 = RT.keyword(null, (String)"lookup");
    public static final Keyword const__14 = RT.keyword(null, (String)"index-top-node");
    public static final Var const__15 = RT.var((String)"datomic.treewalk", (String)"create-parent-node");
    public static final Var const__16 = RT.var((String)"datomic.treewalk", (String)"index-top-walker");
    public static final Keyword const__17 = RT.keyword(null, (String)"log-root-node");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword((String)"backup", (String)"version"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object storage, Object t) {
        Object object;
        Object object2;
        Object or__5238__auto__20241;
        Object object3 = or__5238__auto__20241 = ((IFn)const__0.getRawRoot()).invoke(t, storage);
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = or__5238__auto__20241;
            or__5238__auto__20241 = null;
        } else {
            Object object4 = t;
            t = null;
            object2 = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, ((IFn)const__3.getRawRoot()).invoke((Object)"No database root for t ", object4));
        }
        Object roots = object2;
        Object object5 = storage;
        storage = null;
        Object value_storage = ((IFn)const__4.getRawRoot()).invoke(object5, (Object)"values");
        IFn iFn = (IFn)const__5.getRawRoot();
        Object object6 = value_storage;
        value_storage = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object7 = roots;
        Object object8 = iLookupThunk.get(object7);
        if (iLookupThunk == object8) {
            __thunk__0__ = __site__0__.fault(object7);
            object8 = __thunk__0__.get(object7);
        }
        Object lookup = iFn.invoke(object6, object8);
        Object map__20237 = roots;
        Object object9 = ((IFn)const__7.getRawRoot()).invoke(map__20237);
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = map__20237;
            map__20237 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__8.getRawRoot()).invoke(object10)));
        } else {
            object = map__20237;
            map__20237 = null;
        }
        Object map__202372 = object;
        Object index_root_id = RT.get((Object)map__202372, (Object)const__10);
        Object object11 = map__202372;
        map__202372 = null;
        Object log_root_id = RT.get((Object)object11, (Object)const__11);
        Object object12 = roots;
        roots = null;
        Object object13 = lookup;
        Object object14 = index_root_id;
        index_root_id = null;
        Object object15 = ((IFn)const__15.getRawRoot()).invoke(object14, const__16.getRawRoot(), lookup);
        Object object16 = log_root_id;
        log_root_id = null;
        backup$create_restore_job$fn__20238 backup$create_restore_job$fn__20238 = new backup$create_restore_job$fn__20238(lookup);
        Object object17 = lookup;
        lookup = null;
        return ((IFn)const__12.getRawRoot()).invoke(object12, (Object)const__13, object13, (Object)const__14, object15, (Object)const__17, ((IFn)const__15.getRawRoot()).invoke(object16, (Object)backup$create_restore_job$fn__20238, object17));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return backup$create_restore_job.invokeStatic(object3, object4);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.log$extend_tree$create__16558;
import datomic.log$extend_tree$fn__16560;

public final class log$extend_tree
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"ffirst");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"peek");
    public static final Keyword const__7 = RT.keyword(null, (String)"uuid");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"repeatedly");
    public static final Var const__11 = RT.var((String)"datomic.common", (String)"rand-uuid");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__16 = RT.var((String)"datomic.log", (String)"create-entry");
    public static final Keyword const__17 = RT.keyword(null, (String)"t");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__22 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
    public static final Var const__23 = RT.var((String)"datomic.log", (String)"fressianed-dir");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"deref");
    public static final Keyword const__27 = RT.keyword(null, (String)"root-id");
    public static final Keyword const__28 = RT.keyword(null, (String)"dir-id");
    public static final Keyword const__29 = RT.keyword(null, (String)"leaf-ts");
    public static final Keyword const__30 = RT.keyword(null, (String)"garbage-ids");

    public static Object invokeStatic(Object cs, Object olookup, Object root_id2, Object target_dir_count, Object leaf_segs) {
        Object results;
        Object object;
        Object object2;
        Object new_root_entry;
        Object object3;
        Object object4;
        Object leaf_ts = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), leaf_segs);
        Object leaf_bufs = ((IFn)const__0.getRawRoot()).invoke(const__2.getRawRoot(), leaf_segs);
        Object object5 = leaf_segs;
        leaf_segs = null;
        Object new_seg_t = ((IFn)const__3.getRawRoot()).invoke(object5);
        Object root = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(olookup, root_id2));
        Object old_tail_dir_uuid = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(root), (Object)const__7);
        Object object6 = olookup;
        olookup = null;
        Object tail_dir = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object6, old_tail_dir_uuid));
        Object object7 = target_dir_count;
        target_dir_count = null;
        boolean new_tail_dir_QMARK_ = Numbers.gte((long)RT.count((Object)tail_dir), (Object)object7);
        Object leaf_ids = ((IFn)const__10.getRawRoot()).invoke((Object)RT.count((Object)leaf_bufs), const__11.getRawRoot());
        Object vec__16554 = ((IFn)const__10.getRawRoot()).invoke(const__11.getRawRoot());
        Object new_root_id = RT.nth((Object)vec__16554, (int)RT.intCast((long)0L), null);
        Object object8 = vec__16554;
        vec__16554 = null;
        Object new_dir_id = RT.nth((Object)object8, (int)RT.intCast((long)1L), null);
        IFn iFn = (IFn)const__15.getRawRoot();
        if (new_tail_dir_QMARK_) {
            object4 = PersistentVector.EMPTY;
        } else {
            object4 = tail_dir;
            tail_dir = null;
        }
        Object new_tail_dir = iFn.invoke(object4, ((IFn)const__0.getRawRoot()).invoke(const__16.getRawRoot(), leaf_ts, leaf_ids));
        if (new_tail_dir_QMARK_) {
            Object object9 = new_seg_t;
            new_seg_t = null;
            object3 = ((IFn)const__16.getRawRoot()).invoke(object9, new_dir_id);
        } else {
            object3 = new_root_entry = ((IFn)const__16.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(root), (Object)const__17), new_dir_id);
        }
        if (new_tail_dir_QMARK_) {
            Object object10 = root;
            root = null;
            Object object11 = new_root_entry;
            new_root_entry = null;
            object2 = ((IFn)const__18.getRawRoot()).invoke(object10, object11);
        } else {
            Object object12 = root;
            Object object13 = root;
            root = null;
            Object object14 = new_root_entry;
            new_root_entry = null;
            object2 = ((IFn)const__19.getRawRoot()).invoke(object12, (Object)Numbers.num((long)Numbers.dec((long)RT.count((Object)object13))), object14);
        }
        Object new_root = object2;
        Object object15 = root_id2;
        root_id2 = null;
        IPersistentVector G__16557 = Tuple.create((Object)object15);
        Object object16 = ((IFn)const__21.getRawRoot()).invoke((Object)(new_tail_dir_QMARK_ ? Boolean.TRUE : Boolean.FALSE));
        if (object16 != null && object16 != Boolean.FALSE) {
            IPersistentVector iPersistentVector = G__16557;
            G__16557 = null;
            Object object17 = old_tail_dir_uuid;
            old_tail_dir_uuid = null;
            object = ((IFn)const__18.getRawRoot()).invoke((Object)iPersistentVector, ((IFn)const__22.getRawRoot()).invoke(object17));
        } else {
            object = G__16557;
            G__16557 = null;
        }
        IPersistentVector garbage_ids = object;
        Object object18 = cs;
        cs = null;
        log$extend_tree$create__16558 create2 = new log$extend_tree$create__16558(object18);
        Object object19 = leaf_bufs;
        leaf_bufs = null;
        Object object20 = new_tail_dir;
        new_tail_dir = null;
        Object object21 = new_root;
        new_root = null;
        Object vals = ((IFn)const__18.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object19), ((IFn)const__23.getRawRoot()).invoke(object20), ((IFn)const__23.getRawRoot()).invoke(object21));
        Object object22 = leaf_ids;
        leaf_ids = null;
        Object ids = ((IFn)const__18.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object22), new_dir_id, new_root_id);
        log$extend_tree$create__16558 log$extend_tree$create__16558 = create2;
        create2 = null;
        Object object23 = ids;
        ids = null;
        Object object24 = vals;
        vals = null;
        Object object25 = results = ((IFn)const__24.getRawRoot()).invoke((Object)log$extend_tree$create__16558, object23, object24);
        results = null;
        Object object26 = ((IFn)const__25.getRawRoot()).invoke((Object)new log$extend_tree$fn__16560(), ((IFn)const__0.getRawRoot()).invoke(const__26.getRawRoot(), object25));
        if (object26 == null || object26 == Boolean.FALSE) {
            throw (Throwable)new Error("Write failure extending log tree");
        }
        Object[] objectArray = new Object[8];
        objectArray[0] = const__27;
        Object object27 = new_root_id;
        new_root_id = null;
        objectArray[1] = ((IFn)const__22.getRawRoot()).invoke(object27);
        objectArray[2] = const__28;
        Object object28 = new_dir_id;
        new_dir_id = null;
        objectArray[3] = ((IFn)const__22.getRawRoot()).invoke(object28);
        objectArray[4] = const__29;
        Object object29 = leaf_ts;
        leaf_ts = null;
        objectArray[5] = object29;
        objectArray[6] = const__30;
        IPersistentVector iPersistentVector = garbage_ids;
        garbage_ids = null;
        objectArray[7] = iPersistentVector;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return log$extend_tree.invokeStatic(object6, object7, object8, object9, object10);
    }
}


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
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.slf4j.LoggerFactory
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
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.garbage$append_leaf$create__19793;
import datomic.garbage$append_leaf$fn__19787;
import datomic.garbage$append_leaf$fn__19789;
import datomic.garbage$append_leaf$fn__19791;
import java.util.Date;
import org.slf4j.LoggerFactory;

public final class garbage$append_leaf
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Keyword const__4;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Keyword const__16;
    public static final Var const__17;
    public static final Var const__20;
    public static final AFn const__21;
    public static final Var const__22;
    public static final Keyword const__23;
    public static final Keyword const__24;
    public static final Var const__25;
    public static final Keyword const__26;
    public static final Var const__27;
    public static final Keyword const__28;
    public static final Var const__29;
    public static final AFn const__30;
    public static final AFn const__31;
    public static final AFn const__32;
    public static final Var const__33;
    public static final Keyword const__34;
    public static final Var const__35;
    public static final Keyword const__36;
    public static final Var const__37;
    public static final Var const__38;
    public static final Var const__40;
    public static final Keyword const__41;
    public static final Keyword const__42;
    public static final Keyword const__43;
    public static final Keyword const__44;
    public static final Keyword const__45;
    public static final Keyword const__46;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;
    static final KeywordLookupSite __site__3__;
    static ILookupThunk __thunk__3__;
    static final KeywordLookupSite __site__4__;
    static ILookupThunk __thunk__4__;
    static final KeywordLookupSite __site__5__;
    static ILookupThunk __thunk__5__;
    static final KeywordLookupSite __site__6__;
    static ILookupThunk __thunk__6__;
    static final KeywordLookupSite __site__7__;
    static ILookupThunk __thunk__7__;
    static final KeywordLookupSite __site__8__;
    static ILookupThunk __thunk__8__;
    static final KeywordLookupSite __site__9__;
    static ILookupThunk __thunk__9__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cluster, Object lookup, Object leaf, Object max_dir_size) {
        map__19783 = ((IFn)garbage$append_leaf.const__0.getRawRoot()).invoke(cluster);
        v0 = ((IFn)garbage$append_leaf.const__1.getRawRoot()).invoke(map__19783);
        if (v0 != null && v0 != Boolean.FALSE) {
            v1 = map__19783;
            map__19783 = null;
            v2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)garbage$append_leaf.const__2.getRawRoot()).invoke(v1)));
        } else {
            v2 = map__19783;
            map__19783 = null;
        }
        map__19783 = v2;
        old_root_val_key = RT.get((Object)map__19783, (Object)garbage$append_leaf.const__4);
        v3 = map__19783;
        map__19783 = null;
        old_root_rev = RT.get((Object)v3, (Object)garbage$append_leaf.const__5);
        vec__19784 = ((IFn)garbage$append_leaf.const__6.getRawRoot()).invoke(garbage$append_leaf.const__7.getRawRoot());
        leaf_uuid = RT.nth((Object)vec__19784, (int)RT.intCast((long)0L), null);
        dir_uuid = RT.nth((Object)vec__19784, (int)RT.intCast((long)1L), null);
        v4 = vec__19784;
        vec__19784 = null;
        root_uuid = RT.nth((Object)v4, (int)RT.intCast((long)2L), null);
        v5 = old_root_val_key;
        old_root_val_key = null;
        old_root_id = ((IFn)garbage$append_leaf.const__12.getRawRoot()).invoke(v5);
        old_root_val = ((IFn)garbage$append_leaf.const__13.getRawRoot()).invoke(lookup, old_root_id);
        v6 = (IFn)garbage$append_leaf.const__14.getRawRoot();
        v7 = garbage$append_leaf.__thunk__0__;
        v8 = old_root_val;
        v9 = v7.get(v8);
        if (v7 == v9) {
            garbage$append_leaf.__thunk__0__ = garbage$append_leaf.__site__0__.fault(v8);
            v9 = garbage$append_leaf.__thunk__0__.get(v8);
        }
        v10 = old_dir_id = RT.get((Object)v6.invoke(v9), (Object)garbage$append_leaf.const__16);
        if (v10 != null && v10 != Boolean.FALSE) {
            v11 = lookup;
            lookup = null;
            v12 = RT.get((Object)v11, (Object)old_dir_id);
        } else {
            v12 = null;
        }
        old_dir_val = v12;
        v13 = or__5238__auto__19796 = ((IFn)garbage$append_leaf.const__17.getRawRoot()).invoke(old_dir_val);
        if (v13 != null && v13 != Boolean.FALSE) {
            v14 = or__5238__auto__19796;
            or__5238__auto__19796 = null;
        } else {
            v15 = garbage$append_leaf.__thunk__1__;
            v16 = old_dir_val;
            v17 = v15.get(v16);
            if (v15 == v17) {
                garbage$append_leaf.__thunk__1__ = garbage$append_leaf.__site__1__.fault(v16);
                v17 = garbage$append_leaf.__thunk__1__.get(v16);
            }
            v18 = max_dir_size;
            max_dir_size = null;
            v14 = Numbers.gte((long)RT.count((Object)v17), (Object)v18) != false ? Boolean.TRUE : Boolean.FALSE;
        }
        new_tail_dir_QMARK_ = v14;
        v19 = leaf;
        leaf = null;
        v20 = new Object[4];
        v20[0] = garbage$append_leaf.const__23;
        v20[1] = new Date();
        v20[2] = garbage$append_leaf.const__24;
        v21 = old_dir_id;
        v20[3] = v21 != null && v21 != Boolean.FALSE ? Tuple.create((Object)((IFn)garbage$append_leaf.const__25.getRawRoot()).invoke(old_dir_id), (Object)((IFn)garbage$append_leaf.const__25.getRawRoot()).invoke(old_root_id)) : Tuple.create((Object)((IFn)garbage$append_leaf.const__25.getRawRoot()).invoke(old_root_id));
        leaf = ((IFn)garbage$append_leaf.const__20.getRawRoot()).invoke(v19, (Object)garbage$append_leaf.const__21, garbage$append_leaf.const__22.getRawRoot(), (Object)RT.mapUniqueKeys((Object[])v20));
        v22 = new Object[6];
        v22[0] = garbage$append_leaf.const__26;
        v23 = garbage$append_leaf.__thunk__3__;
        v24 = (IFn)garbage$append_leaf.const__27.getRawRoot();
        v25 = garbage$append_leaf.__thunk__2__;
        v26 = leaf;
        v27 = v25.get(v26);
        if (v25 == v27) {
            garbage$append_leaf.__thunk__2__ = garbage$append_leaf.__site__2__.fault(v26);
            v27 = garbage$append_leaf.__thunk__2__.get(v26);
        }
        v28 = v24.invoke(v27);
        v29 = v23.get(v28);
        if (v23 == v29) {
            garbage$append_leaf.__thunk__3__ = garbage$append_leaf.__site__3__.fault(v28);
            v29 = garbage$append_leaf.__thunk__3__.get(v28);
        }
        v22[1] = v29;
        v22[2] = garbage$append_leaf.const__28;
        v30 = garbage$append_leaf.__thunk__5__;
        v31 = (IFn)garbage$append_leaf.const__14.getRawRoot();
        v32 = garbage$append_leaf.__thunk__4__;
        v33 = leaf;
        v34 = v32.get(v33);
        if (v32 == v34) {
            garbage$append_leaf.__thunk__4__ = garbage$append_leaf.__site__4__.fault(v33);
            v34 = garbage$append_leaf.__thunk__4__.get(v33);
        }
        v35 = v31.invoke(v34);
        v36 = v30.get(v35);
        if (v30 == v36) {
            garbage$append_leaf.__thunk__5__ = garbage$append_leaf.__site__5__.fault(v35);
            v36 = garbage$append_leaf.__thunk__5__.get(v35);
        }
        v22[3] = v36;
        v22[4] = garbage$append_leaf.const__16;
        v22[5] = leaf_uuid;
        dir_entry = RT.mapUniqueKeys((Object[])v22);
        v37 = new Object[6];
        v37[0] = garbage$append_leaf.const__26;
        v38 = new_tail_dir_QMARK_;
        if (v38 != null && v38 != Boolean.FALSE) {
            v39 = garbage$append_leaf.__thunk__6__;
            v40 = dir_entry;
            v41 = v39.get((Object)v40);
            if (v39 == v41) {
                garbage$append_leaf.__thunk__6__ = garbage$append_leaf.__site__6__.fault((Object)v40);
                v41 = garbage$append_leaf.__thunk__6__.get((Object)v40);
            }
        } else {
            v42 = garbage$append_leaf.__thunk__8__;
            v43 = (IFn)garbage$append_leaf.const__27.getRawRoot();
            v44 = garbage$append_leaf.__thunk__7__;
            v45 = old_dir_val;
            v46 = v44.get(v45);
            if (v44 == v46) {
                garbage$append_leaf.__thunk__7__ = garbage$append_leaf.__site__7__.fault(v45);
                v46 = garbage$append_leaf.__thunk__7__.get(v45);
            }
            v47 = v43.invoke(v46);
            v41 = v42.get(v47);
            if (v42 == v41) {
                garbage$append_leaf.__thunk__8__ = garbage$append_leaf.__site__8__.fault(v47);
                v41 = garbage$append_leaf.__thunk__8__.get(v47);
            }
        }
        v37[1] = v41;
        v37[2] = garbage$append_leaf.const__28;
        v48 = garbage$append_leaf.__thunk__9__;
        v49 = dir_entry;
        v50 = v48.get((Object)v49);
        if (v48 == v50) {
            garbage$append_leaf.__thunk__9__ = garbage$append_leaf.__site__9__.fault((Object)v49);
            v50 = garbage$append_leaf.__thunk__9__.get((Object)v49);
        }
        v37[3] = v50;
        v37[4] = garbage$append_leaf.const__16;
        v37[5] = dir_uuid;
        root_entry = RT.mapUniqueKeys((Object[])v37);
        v51 = new_tail_dir_QMARK_;
        if (v51 != null && v51 != Boolean.FALSE) {
            v52 = dir_entry;
            dir_entry = null;
            v53 = ((IFn)garbage$append_leaf.const__29.getRawRoot()).invoke((Object)Tuple.create((Object)v52));
        } else {
            v54 = old_dir_val;
            old_dir_val = null;
            v55 = dir_entry;
            dir_entry = null;
            v53 = ((IFn)garbage$append_leaf.const__20.getRawRoot()).invoke(v54, (Object)garbage$append_leaf.const__30, (Object)new garbage$append_leaf$fn__19787(v55));
        }
        new_dir_val = v53;
        v56 = new_tail_dir_QMARK_;
        new_tail_dir_QMARK_ = null;
        if (v56 != null && v56 != Boolean.FALSE) {
            v57 = old_root_val;
            old_root_val = null;
            v58 = root_entry;
            root_entry = null;
            v59 = ((IFn)garbage$append_leaf.const__20.getRawRoot()).invoke(v57, (Object)garbage$append_leaf.const__31, (Object)new garbage$append_leaf$fn__19789(v58));
        } else {
            v60 = old_root_val;
            old_root_val = null;
            v61 = root_entry;
            root_entry = null;
            v59 = ((IFn)garbage$append_leaf.const__20.getRawRoot()).invoke(v60, (Object)garbage$append_leaf.const__32, (Object)new garbage$append_leaf$fn__19791(v61));
        }
        new_root_val = v59;
        create = new garbage$append_leaf$create__19793(cluster);
        v62 = leaf_uuid;
        leaf_uuid = null;
        v63 = leaf;
        leaf = null;
        leaf_result = ((IFn)create).invoke(v62, v63);
        v64 = new_dir_val;
        new_dir_val = null;
        dir_result = ((IFn)create).invoke(dir_uuid, v64);
        v65 = create;
        create = null;
        v66 = new_root_val;
        new_root_val = null;
        root_result = ((IFn)v65).invoke(root_uuid, v66);
        v67 = leaf_result;
        leaf_result = null;
        v68 = dir_result;
        dir_result = null;
        v69 = root_result;
        root_result = null;
        v70 = ((IFn)garbage$append_leaf.const__33.getRawRoot()).invoke((Object)garbage$append_leaf.const__34, ((IFn)garbage$append_leaf.const__35.getRawRoot()).invoke(v67), ((IFn)garbage$append_leaf.const__35.getRawRoot()).invoke(v68), ((IFn)garbage$append_leaf.const__35.getRawRoot()).invoke(v69));
        if (v70 == null || v70 == Boolean.FALSE) ** GOTO lbl238
        v71 = (IFn)garbage$append_leaf.const__35.getRawRoot();
        v72 = cluster;
        if (Util.classOf((Object)v72) == garbage$append_leaf.__cached_class__0) ** GOTO lbl199
        if (!(v72 instanceof ClusteredStore)) {
            v72 = v72;
            garbage$append_leaf.__cached_class__0 = Util.classOf((Object)v72);
lbl199:
            // 2 sources

            v73 = cluster;
            cluster = null;
            v74 = old_root_rev;
            old_root_rev = null;
            v75 = garbage$append_leaf.const__37.getRawRoot().invoke(v72, ((IFn)garbage$append_leaf.const__38.getRawRoot()).invoke(v73), (Object)Numbers.inc((Object)v74), ((IFn)garbage$append_leaf.const__25.getRawRoot()).invoke(root_uuid));
        } else {
            v76 = cluster;
            cluster = null;
            v77 = old_root_rev;
            old_root_rev = null;
            v75 = ((ClusteredStore)v72).set_ref(((IFn)garbage$append_leaf.const__38.getRawRoot()).invoke(v76), Numbers.inc((Object)v77), ((IFn)garbage$append_leaf.const__25.getRawRoot()).invoke(root_uuid));
        }
        if (Util.equiv((Object)garbage$append_leaf.const__36, (Object)v71.invoke(v75))) {
            logger = LoggerFactory.getLogger((String)"datomic.garbage");
            if (logger.isInfoEnabled()) {
                v78 = logger;
                logger = null;
                v79 = new Object[10];
                v79[0] = garbage$append_leaf.const__41;
                v79[1] = garbage$append_leaf.const__42;
                v79[2] = garbage$append_leaf.const__43;
                v80 = old_root_id;
                old_root_id = null;
                v79[3] = v80;
                v79[4] = garbage$append_leaf.const__44;
                v81 = root_uuid;
                root_uuid = null;
                v79[5] = v81;
                v79[6] = garbage$append_leaf.const__45;
                v82 = old_dir_id;
                old_dir_id = null;
                v79[7] = v82;
                v79[8] = garbage$append_leaf.const__46;
                v83 = dir_uuid;
                dir_uuid = null;
                v79[9] = v83;
                v78.info((String)((IFn)garbage$append_leaf.const__40.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])v79)));
            }
        } else {
            throw (Throwable)new Exception("Conflict");
lbl238:
            // 1 sources

            throw (Throwable)new Exception("Value create failed");
        }
        return garbage$append_leaf.const__36;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return garbage$append_leaf.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"datomic.garbage", (String)"ensure-root-ref");
        const__1 = RT.var((String)"clojure.core", (String)"seq?");
        const__2 = RT.var((String)"clojure.core", (String)"seq");
        const__4 = RT.keyword(null, (String)"key");
        const__5 = RT.keyword(null, (String)"rev");
        const__6 = RT.var((String)"clojure.core", (String)"repeatedly");
        const__7 = RT.var((String)"datomic.common", (String)"rand-uuid");
        const__12 = RT.var((String)"datomic.cluster", (String)"val-key->uuid");
        const__13 = RT.var((String)"datomic.common", (String)"getx");
        const__14 = RT.var((String)"clojure.core", (String)"peek");
        const__16 = RT.keyword(null, (String)"uuid");
        const__17 = RT.var((String)"clojure.core", (String)"not");
        const__20 = RT.var((String)"clojure.core", (String)"update-in");
        const__21 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"children"));
        const__22 = RT.var((String)"clojure.core", (String)"conj");
        const__23 = RT.keyword(null, (String)"tstamp");
        const__24 = RT.keyword(null, (String)"vals");
        const__25 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__26 = RT.keyword(null, (String)"start");
        const__27 = RT.var((String)"clojure.core", (String)"first");
        const__28 = RT.keyword(null, (String)"end");
        const__29 = RT.var((String)"datomic.garbage.fressian", (String)"->GarbageDir");
        const__30 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"children"));
        const__31 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"children"));
        const__32 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"children"));
        const__33 = RT.var((String)"clojure.core", (String)"=");
        const__34 = RT.keyword(null, (String)"created");
        const__35 = RT.var((String)"clojure.core", (String)"deref");
        const__36 = RT.keyword(null, (String)"ok");
        const__37 = RT.var((String)"datomic.cluster", (String)"set-ref");
        const__38 = RT.var((String)"datomic.garbage", (String)"root-ref-key");
        const__40 = RT.var((String)"datomic.slf4j", (String)"process");
        const__41 = RT.keyword(null, (String)"event");
        const__42 = RT.keyword((String)"garbage", (String)"tree");
        const__43 = RT.keyword(null, (String)"root-from");
        const__44 = RT.keyword(null, (String)"root-to");
        const__45 = RT.keyword(null, (String)"dir-from");
        const__46 = RT.keyword(null, (String)"dir-to");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
        __thunk__2__ = __site__2__;
        __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"tstamp"));
        __thunk__3__ = __site__3__;
        __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
        __thunk__4__ = __site__4__;
        __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"tstamp"));
        __thunk__5__ = __site__5__;
        __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"start"));
        __thunk__6__ = __site__6__;
        __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
        __thunk__7__ = __site__7__;
        __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"start"));
        __thunk__8__ = __site__8__;
        __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"end"));
        __thunk__9__ = __site__9__;
    }
}


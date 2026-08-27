/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.fulltext$build_index$fn__14674$fn__14684;
import datomic.fulltext$build_index$fn__14674$fn__14700;
import datomic.fulltext.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class fulltext$build_index$fn__14674
extends AFunction {
    Object olookup;
    Object cstore;
    Object aevt;
    Object db;
    Object old_hist_id;
    Object attrids;
    Object old_root_id;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__10 = RT.var((String)"datomic.fulltext", (String)"write-changed-val");
    public static final Var const__11 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__12 = RT.keyword((String)"index", (String)"fulltext-garbage");
    public static final Keyword const__13 = RT.keyword(null, (String)"garbage-count");
    public static final Keyword const__15 = RT.keyword(null, (String)"threw");

    public fulltext$build_index$fn__14674(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.olookup = object;
        this.cstore = object2;
        this.aevt = object3;
        this.db = object4;
        this.old_hist_id = object5;
        this.attrids = object6;
        this.old_root_id = object7;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object G__14693;
            Object vec__14694;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.aevt = null;
            this.attrids = null;
            Object attriters = ((IFn)const__1.getRawRoot()).invoke((Object)new fulltext$build_index$fn__14674$fn__14684(this.aevt), null, this.attrids);
            Object object = this.old_root_id;
            Object oldroot = object != null && object != Boolean.FALSE ? ((IFn)const__2.getRawRoot()).invoke(this.olookup, this.old_root_id) : null;
            Object object2 = this.old_hist_id;
            Object oldhist = object2 != null && object2 != Boolean.FALSE ? ((IFn)const__2.getRawRoot()).invoke(this.olookup, this.old_hist_id) : null;
            Object object3 = oldroot;
            PersistentArrayMap rootmap = object3 != null && object3 != Boolean.FALSE ? ((Root)oldroot).attrmap : PersistentArrayMap.EMPTY;
            Object object4 = oldhist;
            PersistentArrayMap histmap = object4 != null && object4 != Boolean.FALSE ? ((Root)oldhist).attrmap : PersistentArrayMap.EMPTY;
            PersistentVector garbage2 = PersistentVector.EMPTY;
            Object object5 = attriters;
            attriters = null;
            Object object6 = vec__14694 = (G__14693 = ((IFn)const__3.getRawRoot()).invoke(object5));
            vec__14694 = null;
            Object seq__14695 = ((IFn)const__3.getRawRoot()).invoke(object6);
            Object first__14696 = ((IFn)const__4.getRawRoot()).invoke(seq__14695);
            Object object7 = seq__14695;
            seq__14695 = null;
            Object seq__146952 = ((IFn)const__5.getRawRoot()).invoke(object7);
            Object object8 = first__14696;
            first__14696 = null;
            Object vec__14697 = object8;
            RT.nth((Object)vec__14697, (int)RT.intCast((long)0L), null);
            Object object9 = vec__14697;
            vec__14697 = null;
            RT.nth((Object)object9, (int)RT.intCast((long)1L), null);
            seq__146952 = null;
            this.olookup = null;
            PersistentArrayMap persistentArrayMap = histmap;
            histmap = null;
            Object object10 = G__14693;
            G__14693 = null;
            this.db = null;
            PersistentVector persistentVector = garbage2;
            garbage2 = null;
            PersistentArrayMap persistentArrayMap2 = rootmap;
            rootmap = null;
            Object vec__14675 = ((IFn)new fulltext$build_index$fn__14674$fn__14700(this.olookup, persistentArrayMap, this.cstore, object10, this.db, persistentVector, persistentArrayMap2)).invoke();
            Object root = RT.nth((Object)vec__14675, (int)RT.intCast((long)0L), null);
            Object hist = RT.nth((Object)vec__14675, (int)RT.intCast((long)1L), null);
            Object object11 = vec__14675;
            vec__14675 = null;
            Object garbage3 = RT.nth((Object)object11, (int)RT.intCast((long)2L), null);
            this.old_root_id = null;
            Object object12 = oldroot;
            oldroot = null;
            Object object13 = root;
            root = null;
            Object object14 = garbage3;
            garbage3 = null;
            Object vec__14678 = ((IFn)const__10.getRawRoot()).invoke(this.cstore, this.old_root_id, object12, object13, object14);
            Object new_root_id = RT.nth((Object)vec__14678, (int)RT.intCast((long)0L), null);
            Object object15 = vec__14678;
            vec__14678 = null;
            Object garbage4 = RT.nth((Object)object15, (int)RT.intCast((long)1L), null);
            this.cstore = null;
            this.old_hist_id = null;
            Object object16 = oldhist;
            oldhist = null;
            Object object17 = hist;
            hist = null;
            Object object18 = garbage4;
            garbage4 = null;
            Object vec__14681 = ((IFn)const__10.getRawRoot()).invoke(this.cstore, this.old_hist_id, object16, object17, object18);
            Object new_hist_id = RT.nth((Object)vec__14681, (int)RT.intCast((long)0L), null);
            Object object19 = vec__14681;
            vec__14681 = null;
            Object garbage5 = RT.nth((Object)object19, (int)RT.intCast((long)1L), null);
            Logger logger = LoggerFactory.getLogger((String)"datomic.fulltext");
            if (logger.isDebugEnabled()) {
                Logger logger2 = logger;
                logger = null;
                logger2.debug((String)((IFn)const__11.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])new Object[]{const__12, RT.mapUniqueKeys((Object[])new Object[]{const__13, RT.count((Object)garbage5)})})));
            }
            Object object20 = new_root_id;
            new_root_id = null;
            Object object21 = new_hist_id;
            new_hist_id = null;
            Object object22 = garbage5;
            garbage5 = null;
            objectArray[1] = Tuple.create((Object)object20, (Object)object21, (Object)object22);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__15;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}


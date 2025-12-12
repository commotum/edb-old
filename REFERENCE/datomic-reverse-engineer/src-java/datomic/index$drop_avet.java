/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$drop_avet$fn__15598;
import datomic.index.RootNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$drop_avet
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"getx");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final Keyword const__4 = RT.keyword(null, (String)"keydata");
    public static final Keyword const__5 = RT.keyword(null, (String)"dirids");
    public static final Keyword const__6 = RT.keyword(null, (String)"garbage");
    public static final AFn const__7 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"keydata"), (Object)RT.keyword(null, (String)"dirids"), (Object)RT.keyword(null, (String)"garbage"));
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"repeat");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__13 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__14 = RT.keyword(null, (String)"event");
    public static final Keyword const__15 = RT.keyword((String)"index", (String)"drop-avet-segments");
    public static final Keyword const__16 = RT.keyword(null, (String)"aid");
    public static final Keyword const__17 = RT.keyword(null, (String)"count");
    public static final Var const__19 = RT.var((String)"datomic.index", (String)"write-object");
    public static final Var const__20 = RT.var((String)"datomic.index", (String)"root-node");
    public static final Var const__21 = RT.var((String)"datomic.index", (String)"transpose");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"into");

    public static Object invokeStatic(Object store, Object olookup, Object old_rootid, Object aid, Object old_garbage) {
        IPersistentVector iPersistentVector;
        Object object;
        Object oldroot = ((IFn)const__0.getRawRoot()).invoke(olookup, old_rootid);
        int ct = RT.count((Object)((RootNode)oldroot).keydata);
        index$drop_avet$fn__15598 index$drop_avet$fn__15598 = new index$drop_avet$fn__15598(aid, oldroot, ct, olookup, store);
        Object object2 = oldroot;
        oldroot = null;
        Object map__15597 = ((IFn)const__2.getRawRoot()).invoke((Object)index$drop_avet$fn__15598, ((IFn)const__3.getRawRoot()).invoke((Object)const__7, ((IFn)const__8.getRawRoot()).invoke((Object)PersistentVector.EMPTY)), ((IFn)const__9.getRawRoot()).invoke((Object)RT.count((Object)((RootNode)object2).keydata)));
        Object object3 = ((IFn)const__10.getRawRoot()).invoke(map__15597);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__15597;
            map__15597 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__11.getRawRoot()).invoke(object4)));
        } else {
            object = map__15597;
            map__15597 = null;
        }
        Object map__155972 = object;
        Object keydata = RT.get((Object)map__155972, (Object)const__4);
        Object dirids = RT.get((Object)map__155972, (Object)const__5);
        Object object5 = map__155972;
        map__155972 = null;
        Object garbage2 = RT.get((Object)object5, (Object)const__6);
        Object object6 = ((IFn)const__11.getRawRoot()).invoke(garbage2);
        if (object6 != null && object6 != Boolean.FALSE) {
            Logger logger = LoggerFactory.getLogger((String)"datomic.index");
            if (logger.isInfoEnabled()) {
                Logger logger2 = logger;
                logger = null;
                Object[] objectArray = new Object[6];
                objectArray[0] = const__14;
                objectArray[1] = const__15;
                objectArray[2] = const__16;
                Object object7 = aid;
                aid = null;
                objectArray[3] = object7;
                objectArray[4] = const__17;
                objectArray[5] = Numbers.num((long)Numbers.unchecked_inc((long)RT.count((Object)garbage2)));
                logger2.info((String)((IFn)const__13.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
            }
            Object object8 = store;
            store = null;
            Object object9 = olookup;
            olookup = null;
            Object object10 = keydata;
            keydata = null;
            Object object11 = dirids;
            dirids = null;
            Object object12 = old_garbage;
            old_garbage = null;
            Object object13 = garbage2;
            garbage2 = null;
            Object object14 = old_rootid;
            old_rootid = null;
            iPersistentVector = Tuple.create((Object)((IFn)const__19.getRawRoot()).invoke(object8, object9, ((IFn)const__20.getRawRoot()).invoke(((IFn)const__21.getRawRoot()).invoke(object10), ((IFn)const__22.getRawRoot()).invoke(object11))), (Object)((IFn)const__23.getRawRoot()).invoke(((IFn)const__24.getRawRoot()).invoke(object12, object13), object14));
        } else {
            Object object15 = old_rootid;
            old_rootid = null;
            Object object16 = old_garbage;
            old_garbage = null;
            iPersistentVector = Tuple.create((Object)object15, (Object)object16);
        }
        return iPersistentVector;
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
        return index$drop_avet.invokeStatic(object6, object7, object8, object9, object10);
    }
}


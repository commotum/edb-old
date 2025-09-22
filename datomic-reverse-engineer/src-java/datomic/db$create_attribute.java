/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$create_attribute$fn__13118;
import datomic.db$create_attribute$fn__13120;
import datomic.db$create_attribute$fn__13123;
import datomic.db$create_attribute$norm__13116;

public final class db$create_attribute
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"cardinality");
    public static final Keyword const__4 = RT.keyword(null, (String)"attrPreds");
    public static final Keyword const__5 = RT.keyword(null, (String)"unique");
    public static final Keyword const__6 = RT.keyword(null, (String)"vtypeid");
    public static final Keyword const__7 = RT.keyword(null, (String)"index");
    public static final Keyword const__8 = RT.keyword(null, (String)"storageHasAVET");
    public static final Keyword const__9 = RT.keyword(null, (String)"tupleType");
    public static final Keyword const__10 = RT.keyword(null, (String)"tupleTypes");
    public static final Keyword const__11 = RT.keyword(null, (String)"fulltext");
    public static final Keyword const__12 = RT.keyword(null, (String)"noHistory");
    public static final Keyword const__13 = RT.keyword(null, (String)"isComponent");
    public static final Keyword const__14 = RT.keyword(null, (String)"kw");
    public static final Keyword const__15 = RT.keyword(null, (String)"needsAVET");
    public static final Keyword const__16 = RT.keyword(null, (String)"id");
    public static final Keyword const__17 = RT.keyword(null, (String)"tupleAttrs");
    public static final Keyword const__19 = RT.keyword((String)"db.type", (String)"ref");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"range");
    public static final Object const__21 = 8L;
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"keep-indexed");
    public static final Var const__23 = RT.var((String)"datomic.db", (String)"map->Attribute");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__25 = RT.keyword(null, (String)"attrPred");
    public static final Keyword const__26 = RT.keyword(null, (String)"tupleRefOffsets");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"set");

    public static Object invokeStatic(Object db2, Object p__13114) {
        Object object;
        Object object2;
        Object object3;
        Object object4;
        Object object5;
        Object object6;
        Object object7;
        Object object8 = p__13114;
        p__13114 = null;
        Object map__13115 = object8;
        Object object9 = ((IFn)const__0.getRawRoot()).invoke(map__13115);
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = map__13115;
            map__13115 = null;
            object7 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object10)));
        } else {
            object7 = map__13115;
            map__13115 = null;
        }
        Object map__131152 = object7;
        Object cardinality = RT.get((Object)map__131152, (Object)const__3);
        Object attrPreds = RT.get((Object)map__131152, (Object)const__4);
        Object unique = RT.get((Object)map__131152, (Object)const__5);
        Object vtypeid = RT.get((Object)map__131152, (Object)const__6);
        Object index2 = RT.get((Object)map__131152, (Object)const__7);
        Object storageHasAVET = RT.get((Object)map__131152, (Object)const__8);
        Object tupleType = RT.get((Object)map__131152, (Object)const__9);
        Object tupleTypes = RT.get((Object)map__131152, (Object)const__10);
        Object fulltext2 = RT.get((Object)map__131152, (Object)const__11);
        Object noHistory = RT.get((Object)map__131152, (Object)const__12);
        Object isComponent = RT.get((Object)map__131152, (Object)const__13);
        Object kw = RT.get((Object)map__131152, (Object)const__14);
        Object needsAVET = RT.get((Object)map__131152, (Object)const__15);
        Object id = RT.get((Object)map__131152, (Object)const__16);
        Object object11 = map__131152;
        map__131152 = null;
        Object tupleAttrs = RT.get((Object)object11, (Object)const__17);
        db$create_attribute$norm__13116 norm = new db$create_attribute$norm__13116();
        if (Util.equiv((Object)const__19, (Object)tupleType)) {
            object6 = ((IFn)const__20.getRawRoot()).invoke(const__21);
        } else {
            Object object12 = tupleTypes;
            if (object12 != null && object12 != Boolean.FALSE) {
                object6 = ((IFn)const__22.getRawRoot()).invoke((Object)new db$create_attribute$fn__13118(), tupleTypes);
            } else {
                Object object13 = tupleAttrs;
                if (object13 != null && object13 != Boolean.FALSE) {
                    Object object14 = db2;
                    db2 = null;
                    object6 = ((IFn)const__22.getRawRoot()).invoke((Object)new db$create_attribute$fn__13120(object14), tupleAttrs);
                } else {
                    object6 = null;
                }
            }
        }
        Object tupleRefOffsets = object6;
        Object[] objectArray = new Object[22];
        objectArray[0] = const__5;
        Object object15 = unique;
        unique = null;
        objectArray[1] = object15;
        objectArray[2] = const__6;
        Object object16 = vtypeid;
        vtypeid = null;
        objectArray[3] = object16;
        objectArray[4] = const__8;
        Object object17 = storageHasAVET;
        storageHasAVET = null;
        objectArray[5] = ((IFn)norm).invoke(object17);
        objectArray[6] = const__7;
        Object object18 = index2;
        index2 = null;
        objectArray[7] = ((IFn)norm).invoke(object18);
        objectArray[8] = const__11;
        Object object19 = fulltext2;
        fulltext2 = null;
        objectArray[9] = ((IFn)norm).invoke(object19);
        objectArray[10] = const__12;
        Object object20 = noHistory;
        noHistory = null;
        objectArray[11] = ((IFn)norm).invoke(object20);
        objectArray[12] = const__13;
        Object object21 = isComponent;
        isComponent = null;
        objectArray[13] = ((IFn)norm).invoke(object21);
        objectArray[14] = const__14;
        objectArray[15] = kw;
        objectArray[16] = const__15;
        db$create_attribute$norm__13116 db$create_attribute$norm__13116 = norm;
        norm = null;
        Object object22 = needsAVET;
        needsAVET = null;
        objectArray[17] = ((IFn)db$create_attribute$norm__13116).invoke(object22);
        objectArray[18] = const__16;
        Object object23 = id;
        id = null;
        objectArray[19] = object23;
        objectArray[20] = const__3;
        Object object24 = cardinality;
        cardinality = null;
        objectArray[21] = object24;
        Object G__13122 = ((IFn)const__23.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
        Object object25 = attrPreds;
        if (object25 != null && object25 != Boolean.FALSE) {
            Object object26 = G__13122;
            G__13122 = null;
            Object object27 = kw;
            kw = null;
            Object object28 = attrPreds;
            attrPreds = null;
            object5 = ((IFn)const__24.getRawRoot()).invoke(object26, (Object)const__25, (Object)new Delay((IFn)new db$create_attribute$fn__13123(object27, object28)));
        } else {
            object5 = G__13122;
            G__13122 = null;
        }
        Object G__131222 = object5;
        Object object29 = tupleType;
        if (object29 != null && object29 != Boolean.FALSE) {
            Object object30 = G__131222;
            G__131222 = null;
            Object object31 = tupleType;
            tupleType = null;
            object4 = ((IFn)const__24.getRawRoot()).invoke(object30, (Object)const__9, object31);
        } else {
            object4 = G__131222;
            G__131222 = null;
        }
        Object G__131223 = object4;
        Object object32 = tupleTypes;
        if (object32 != null && object32 != Boolean.FALSE) {
            Object object33 = G__131223;
            G__131223 = null;
            Object object34 = tupleTypes;
            tupleTypes = null;
            object3 = ((IFn)const__24.getRawRoot()).invoke(object33, (Object)const__10, object34);
        } else {
            object3 = G__131223;
            G__131223 = null;
        }
        Object G__131224 = object3;
        Object object35 = tupleRefOffsets;
        if (object35 != null && object35 != Boolean.FALSE) {
            Object object36 = G__131224;
            G__131224 = null;
            Object object37 = tupleRefOffsets;
            tupleRefOffsets = null;
            object2 = ((IFn)const__24.getRawRoot()).invoke(object36, (Object)const__26, ((IFn)const__27.getRawRoot()).invoke(object37));
        } else {
            object2 = G__131224;
            G__131224 = null;
        }
        Object G__131225 = object2;
        Object object38 = tupleAttrs;
        if (object38 != null && object38 != Boolean.FALSE) {
            Object object39 = G__131225;
            G__131225 = null;
            Object object40 = tupleAttrs;
            tupleAttrs = null;
            object = ((IFn)const__24.getRawRoot()).invoke(object39, (Object)const__17, object40);
        } else {
            object = G__131225;
            G__131225 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$create_attribute.invokeStatic(object3, object4);
    }
}


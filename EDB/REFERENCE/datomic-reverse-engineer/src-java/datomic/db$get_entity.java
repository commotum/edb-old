/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$get_entity$fn__13016;
import datomic.db.Attribute;
import datomic.db.IDb;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;

public final class db$get_entity
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"raw");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__7 = RT.keyword(null, (String)"e");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"get-entity");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"resolve-kw");
    public static final Keyword const__13 = RT.keyword(null, (String)"else");
    public static final Var const__14 = RT.var((String)"datomic.iter", (String)"inext");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__16 = RT.keyword((String)"db", (String)"id");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"conj");

    public static Object invokeStatic(Object db2, Object ent, ISeq p__13014) {
        ISeq map__13015;
        ISeq iSeq;
        ISeq iSeq2 = p__13014;
        p__13014 = null;
        ISeq map__130152 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__130152);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__130152;
            map__130152 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__130152;
            map__130152 = null;
        }
        ISeq iSeq4 = map__13015 = iSeq;
        map__13015 = null;
        Object raw = RT.get((Object)iSeq4, (Object)const__3);
        Object object2 = ent;
        ent = null;
        Object eid = ((IFn)const__4.getRawRoot()).invoke(db2, object2);
        Object iter2 = ((IFn)const__5.getRawRoot()).invoke(db2, (Object)new db$get_entity$fn__13016(eid), (Object)((IDb)db2).seekEAVT((IDatum)((IFn)const__6.getRawRoot()).invoke(db2, (Object)const__7, eid)));
        Object ret = null;
        while (true) {
            Object object3;
            Object object4;
            Object and__5236__auto__13024;
            IPersistentMap iPersistentMap;
            IPersistentMap or__5238__auto__13023;
            Object object5;
            Object object6;
            Object and__5236__auto__13019;
            Object attr;
            Object object7 = iter2;
            if (object7 == null || object7 == Boolean.FALSE) break;
            Object d = ((IFn)const__8.getRawRoot()).invoke(iter2);
            int attrid = ((IDatum)d).getA();
            Object object8 = attr = ((IDbImpl)db2).elementAt(attrid);
            Object attrk = object8 != null && object8 != Boolean.FALSE ? ((Attribute)attr).kw() : ((IDb)db2).keywordOf(attrid);
            Object object9 = d;
            d = null;
            Object v = ((IDatum)object9).getV();
            Object object10 = and__5236__auto__13019 = attr;
            if (object10 != null && object10 != Boolean.FALSE) {
                object6 = ((Attribute)attr).vtypeid;
            } else {
                object6 = and__5236__auto__13019;
                and__5236__auto__13019 = null;
            }
            Object vtypeid = object6;
            Object object11 = raw;
            if (object11 != null && object11 != Boolean.FALSE) {
                object5 = v;
                v = null;
            } else {
                Object object12;
                Object and__5236__auto__13020;
                Object object13 = and__5236__auto__13020 = attr;
                if (object13 != null && object13 != Boolean.FALSE) {
                    object12 = ((Attribute)attr).isComponent;
                } else {
                    object12 = and__5236__auto__13020;
                    and__5236__auto__13020 = null;
                }
                if (object12 != null && object12 != Boolean.FALSE) {
                    Object object14 = v;
                    v = null;
                    object5 = ((IFn)const__9.getRawRoot()).invoke(db2, object14);
                } else {
                    Object object15;
                    Object and__5236__auto__13021;
                    Object object16 = and__5236__auto__13021 = vtypeid;
                    if (object16 != null && object16 != Boolean.FALSE) {
                        Object object17 = vtypeid;
                        vtypeid = null;
                        object15 = Util.equiv((Object)object17, (long)20L) ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        object15 = and__5236__auto__13021;
                        and__5236__auto__13021 = null;
                    }
                    if (object15 != null && object15 != Boolean.FALSE) {
                        Object or__5238__auto__13022;
                        Object object18 = or__5238__auto__13022 = ((IFn)const__12.getRawRoot()).invoke(db2, v);
                        if (object18 != null && object18 != Boolean.FALSE) {
                            object5 = or__5238__auto__13022;
                            or__5238__auto__13022 = null;
                        } else {
                            object5 = v;
                            v = null;
                        }
                    } else {
                        Keyword keyword = const__13;
                        if (keyword != null && keyword != Boolean.FALSE) {
                            object5 = v;
                            v = null;
                        } else {
                            object5 = null;
                        }
                    }
                }
            }
            Object val = object5;
            Object object19 = iter2;
            iter2 = null;
            Object object20 = ((IFn)const__14.getRawRoot()).invoke(object19);
            IFn iFn = (IFn)const__15.getRawRoot();
            IPersistentMap iPersistentMap2 = or__5238__auto__13023 = ret;
            if (iPersistentMap2 != null && iPersistentMap2 != Boolean.FALSE) {
                iPersistentMap = or__5238__auto__13023;
                or__5238__auto__13023 = null;
            } else {
                iPersistentMap = RT.mapUniqueKeys((Object[])new Object[]{const__16, eid});
            }
            Object object21 = and__5236__auto__13024 = attr;
            if (object21 != null && object21 != Boolean.FALSE) {
                Object object22 = attr;
                attr = null;
                object4 = Util.equiv((long)36L, (Object)((Attribute)object22).cardinality) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                object4 = and__5236__auto__13024;
                and__5236__auto__13024 = null;
            }
            if (object4 != null && object4 != Boolean.FALSE) {
                IPersistentMap iPersistentMap3 = ret;
                ret = null;
                Object object23 = attrk;
                attrk = null;
                Object object24 = val;
                val = null;
                object3 = ((IFn)const__18.getRawRoot()).invoke(RT.get((Object)iPersistentMap3, (Object)object23, (Object)PersistentHashSet.EMPTY), object24);
            } else {
                object3 = val;
                val = null;
            }
            ret = iFn.invoke((Object)iPersistentMap, attrk, object3);
            iter2 = object20;
        }
        IPersistentMap iPersistentMap = ret;
        ret = null;
        return iPersistentMap;
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return db$get_entity.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}


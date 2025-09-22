/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
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
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$fn__18233$bind__18245;
import datomic.datalog$fn__18233$fn__18240;
import datomic.datalog$fn__18233$fn__18242;
import datomic.datalog$fn__18233$fn__18251;
import datomic.datalog$fn__18233$fn__18253;
import datomic.datalog$fn__18233$fn__18261;
import datomic.datalog$fn__18233$fn__18263;
import datomic.datalog$fn__18233$fn__18275;
import datomic.datalog$fn__18233$fn__18277;
import datomic.datalog$fn__18233$fn__18283;
import datomic.datalog$fn__18233$fn__18285;
import datomic.datalog$fn__18233$fn__18291;
import datomic.datalog$fn__18233$fn__18293;
import datomic.datalog$fn__18233$fn__18298;
import datomic.datalog$fn__18233$fn__18300;
import datomic.datalog$fn__18233$fn__18304;
import datomic.datalog$fn__18233$fn__18306;
import datomic.datalog$fn__18233$fn__18310;
import datomic.datalog$fn__18233$fn__18312;
import datomic.datalog$fn__18233$fn__18332;
import datomic.datalog$fn__18233$fn__18337;
import datomic.datalog$fn__18233$hashx__18328;
import datomic.datalog$fn__18233$hashy__18330;
import datomic.datalog$fn__18233$ident__18321;
import datomic.datalog$fn__18233$join__18334;
import datomic.datalog$fn__18233$match_QMARK___18326;
import datomic.datalog$fn__18233$project__18316;
import datomic.datalog.DbRel;
import datomic.db.Attribute;
import datomic.db.IDbImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class datalog$fn__18233
extends AFunction {
    public static final Object const__1 = 1L;
    public static final Object const__2 = 2L;
    public static final Object const__3 = 0L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"range");
    public static final Object const__12 = 5L;
    public static final Keyword const__17 = RT.keyword(null, (String)"else");
    public static final Var const__19 = RT.var((String)"datomic.datalog", (String)"iterator");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__30 = RT.var((String)"datomic.datalog", (String)"qmapv");

    public static Object invokeStatic(Object dbrel2, Object ys, Object join_map, Object project_map_x, Object project_map_y, Object predctor) {
        Object object;
        Object object2;
        Object object3;
        Object and__5236__auto__18342;
        Object object4;
        Object const_attr;
        Object and__5236__auto__18341;
        Object const_attrid;
        Object db2 = ((DbRel)dbrel2).db;
        Object consts = ((DbRel)dbrel2).consts;
        Object starts = ((DbRel)dbrel2).starts;
        Object whiles = ((DbRel)dbrel2).whiles;
        Object object5 = const_attrid = RT.get((Object)consts, (Object)const__1);
        Object object6 = and__5236__auto__18341 = (const_attr = object5 != null && object5 != Boolean.FALSE ? ((IDbImpl)db2).elementAt(const_attrid) : null);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object and__5236__auto__18340;
            Object object7 = and__5236__auto__18340 = ((Attribute)const_attr).hasAVET();
            if (object7 != null && object7 != Boolean.FALSE) {
                object4 = RT.get((Object)starts, (Object)const__2);
            } else {
                object4 = and__5236__auto__18340;
                and__5236__auto__18340 = null;
            }
        } else {
            object4 = and__5236__auto__18341;
            and__5236__auto__18341 = null;
        }
        Object startv = object4;
        Object whilev = RT.get((Object)whiles, (Object)const__2);
        Object object8 = and__5236__auto__18342 = const_attr;
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = starts;
            starts = null;
            object3 = RT.get((Object)object9, (Object)const__3);
        } else {
            object3 = and__5236__auto__18342;
            and__5236__auto__18342 = null;
        }
        Object starte = object3;
        Object object10 = whiles;
        whiles = null;
        Object whilee = RT.get((Object)object10, (Object)const__3);
        Object px_from = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(project_map_x));
        Object object11 = project_map_x;
        project_map_x = null;
        Object px_to = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object11));
        Object py_from = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(project_map_y));
        Object object12 = project_map_y;
        project_map_y = null;
        Object py_to = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(object12));
        int proj_count = RT.count((Object)((IFn)const__8.getRawRoot()).invoke(((IFn)const__9.getRawRoot()).invoke(px_to), py_to));
        Object object13 = join_map;
        join_map = null;
        Object bindings = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__10.getRawRoot()).invoke((Object)new datalog$fn__18233$fn__18240(object13), ((IFn)const__11.getRawRoot()).invoke(const__12)));
        Object[] bound = RT.object_array((Object)const__12);
        long n__5742__auto__18343 = 5L;
        ((IFn)new datalog$fn__18233$fn__18242(bindings, consts, bound, n__5742__auto__18343)).invoke();
        Object object14 = consts;
        consts = null;
        datalog$fn__18233$bind__18245 bind = new datalog$fn__18233$bind__18245(bindings, const_attrid, object14, dbrel2, db2);
        Object object15 = RT.aget((Object[])bound, (int)((int)1L));
        if (object15 != null && object15 != Boolean.FALSE) {
            Object object16 = RT.aget((Object[])bound, (int)((int)0L));
            if (object16 != null && object16 != Boolean.FALSE) {
                Object object17 = startv;
                startv = null;
                Object object18 = whilev;
                whilev = null;
                object2 = Tuple.create((Object)((Object)new datalog$fn__18233$fn__18251(object17, bound)), (Object)((Object)new datalog$fn__18233$fn__18253(bound, object18, db2)));
            } else {
                Object object19 = RT.aget((Object[])bound, (int)((int)2L));
                if (object19 != null && object19 != Boolean.FALSE) {
                    object2 = Tuple.create((Object)((Object)new datalog$fn__18233$fn__18261()), (Object)((Object)new datalog$fn__18233$fn__18263(db2)));
                } else {
                    Object object20 = startv;
                    if (object20 != null && object20 != Boolean.FALSE) {
                        Object object21 = startv;
                        startv = null;
                        Object object22 = const_attrid;
                        const_attrid = null;
                        Object object23 = whilev;
                        whilev = null;
                        object2 = Tuple.create((Object)((Object)new datalog$fn__18233$fn__18275(object21)), (Object)((Object)new datalog$fn__18233$fn__18277(object22, object23, db2)));
                    } else {
                        Object object24 = starte;
                        if (object24 != null && object24 != Boolean.FALSE) {
                            Object object25 = starte;
                            starte = null;
                            Object object26 = const_attrid;
                            const_attrid = null;
                            Object object27 = whilee;
                            whilee = null;
                            object2 = Tuple.create((Object)((Object)new datalog$fn__18233$fn__18283(object25)), (Object)((Object)new datalog$fn__18233$fn__18285(object26, object27, db2)));
                        } else {
                            Object object28;
                            Object and__5236__auto__18345;
                            Object object29 = and__5236__auto__18345 = whilev;
                            if (object29 != null && object29 != Boolean.FALSE) {
                                Object and__5236__auto__18344;
                                Object object30 = and__5236__auto__18344 = const_attr;
                                if (object30 != null && object30 != Boolean.FALSE) {
                                    Object object31 = const_attr;
                                    const_attr = null;
                                    object28 = ((Attribute)object31).hasAVET();
                                } else {
                                    object28 = and__5236__auto__18344;
                                    and__5236__auto__18344 = null;
                                }
                            } else {
                                object28 = and__5236__auto__18345;
                                and__5236__auto__18345 = null;
                            }
                            if (object28 != null && object28 != Boolean.FALSE) {
                                Object object32 = const_attrid;
                                const_attrid = null;
                                Object object33 = whilev;
                                whilev = null;
                                object2 = Tuple.create((Object)((Object)new datalog$fn__18233$fn__18291()), (Object)((Object)new datalog$fn__18233$fn__18293(object32, object33, db2)));
                            } else {
                                Keyword keyword = const__17;
                                object2 = keyword != null && keyword != Boolean.FALSE ? Tuple.create((Object)((Object)new datalog$fn__18233$fn__18298()), (Object)((Object)new datalog$fn__18233$fn__18300(db2))) : null;
                            }
                        }
                    }
                }
            }
        } else {
            Object object34 = RT.aget((Object[])bound, (int)((int)0L));
            if (object34 != null && object34 != Boolean.FALSE) {
                object2 = Tuple.create((Object)((Object)new datalog$fn__18233$fn__18304()), (Object)((Object)new datalog$fn__18233$fn__18306(db2)));
            } else {
                Object object35 = RT.aget((Object[])bound, (int)((int)2L));
                if (object35 != null && object35 != Boolean.FALSE) {
                    object2 = Tuple.create((Object)((Object)new datalog$fn__18233$fn__18310()), (Object)((Object)new datalog$fn__18233$fn__18312(db2)));
                } else {
                    Keyword keyword = const__17;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        throw (Throwable)new Exception("Insufficient bindings, will cause db scan");
                    }
                    object2 = null;
                }
            }
        }
        IPersistentVector vec__18234 = object2;
        Object prober = RT.nth((Object)vec__18234, (int)RT.uncheckedIntCast((long)0L), null);
        IPersistentVector iPersistentVector = vec__18234;
        vec__18234 = null;
        Object probe = RT.nth((Object)iPersistentVector, (int)RT.uncheckedIntCast((long)1L), null);
        Object object36 = px_to;
        px_to = null;
        Object object37 = py_to;
        py_to = null;
        Object object38 = px_from;
        px_from = null;
        Object object39 = py_from;
        py_from = null;
        datalog$fn__18233$project__18316 project2 = new datalog$fn__18233$project__18316(object36, object37, proj_count, object38, object39);
        Object object40 = dbrel2;
        dbrel2 = null;
        Object object41 = db2;
        db2 = null;
        datalog$fn__18233$ident__18321 ident2 = new datalog$fn__18233$ident__18321(object40, object41);
        Object object42 = bindings;
        bindings = null;
        datalog$fn__18233$match_QMARK___18326 match_QMARK_ = new datalog$fn__18233$match_QMARK___18326(object42, (Object)ident2);
        datalog$fn__18233$hashx__18328 hashx = new datalog$fn__18233$hashx__18328(bound, (Object)ident2);
        Object[] objectArray = bound;
        bound = null;
        datalog$fn__18233$ident__18321 datalog$fn__18233$ident__18321 = ident2;
        ident2 = null;
        datalog$fn__18233$hashy__18330 hashy = new datalog$fn__18233$hashy__18330(objectArray, (Object)datalog$fn__18233$ident__18321);
        HashMap ht = new HashMap();
        HashSet<Object> ps = new HashSet<Object>();
        Object object43 = ys;
        ys = null;
        Object yiter = ((IFn)const__19.getRawRoot()).invoke(object43);
        while (((Iterator)yiter).hasNext()) {
            Object vs;
            Object object44;
            Object or__5238__auto__18346;
            Object y = ((Iterator)yiter).next();
            Object bindy = ((IFn)bind).invoke(y);
            Object h = ((IFn)hashy).invoke(bindy);
            Object v = or__5238__auto__18346 = ht.get(h);
            if (v != null && v != Boolean.FALSE) {
                object44 = or__5238__auto__18346;
                or__5238__auto__18346 = null;
            } else {
                ArrayList vs2 = new ArrayList(RT.uncheckedIntCast((long)2L));
                Object object45 = h;
                h = null;
                ht.put(object45, vs2);
                object44 = vs2;
                vs2 = null;
            }
            Object v2 = vs = object44;
            vs = null;
            Object e = y;
            y = null;
            Boolean bl = ((ArrayList)v2).add(e) ? Boolean.TRUE : Boolean.FALSE;
            Object object46 = bindy;
            bindy = null;
            Boolean bl2 = ps.add(((IFn)prober).invoke(object46)) ? Boolean.TRUE : Boolean.FALSE;
        }
        HashMap hashMap = ht;
        ht = null;
        HashSet<Object> hashSet = ps;
        ps = null;
        IPersistentVector vec__18237 = Tuple.create(hashMap, hashSet);
        Object ht2 = RT.nth((Object)vec__18237, (int)RT.uncheckedIntCast((long)0L), null);
        IPersistentVector iPersistentVector2 = vec__18237;
        vec__18237 = null;
        Object probeset = RT.nth((Object)iPersistentVector2, (int)RT.uncheckedIntCast((long)1L), null);
        long PAR = 4L;
        int cnt = RT.count((Object)probeset);
        if ((long)cnt > 100L) {
            Object object47 = probeset;
            probeset = null;
            ArrayList palist = new ArrayList((Collection)object47);
            long sz = (long)cnt % PAR == 0L ? (long)cnt / PAR : (long)cnt / PAR + 1L;
            ArrayList arrayList = palist;
            palist = null;
            object = ((IFn)const__27.getRawRoot()).invoke((Object)new datalog$fn__18233$fn__18332(sz, cnt, arrayList), ((IFn)const__11.getRawRoot()).invoke((Object)Numbers.num((long)PAR)));
        } else {
            Object object48 = probeset;
            probeset = null;
            object = Tuple.create((Object)object48);
        }
        IPersistentVector probelists = object;
        Object object49 = predctor;
        predctor = null;
        datalog$fn__18233$hashx__18328 datalog$fn__18233$hashx__18328 = hashx;
        hashx = null;
        datalog$fn__18233$match_QMARK___18326 datalog$fn__18233$match_QMARK___18326 = match_QMARK_;
        match_QMARK_ = null;
        datalog$fn__18233$project__18316 datalog$fn__18233$project__18316 = project2;
        project2 = null;
        Object object50 = ht2;
        ht2 = null;
        Object object51 = probe;
        probe = null;
        datalog$fn__18233$join__18334 join = new datalog$fn__18233$join__18334(object49, (Object)datalog$fn__18233$hashx__18328, (Object)datalog$fn__18233$match_QMARK___18326, (Object)datalog$fn__18233$project__18316, object50, object51);
        Set ret = Collections.newSetFromMap(new ConcurrentHashMap(RT.uncheckedIntCast((long)16L), (float)0.75, RT.uncheckedIntCast((long)PAR)));
        datalog$fn__18233$join__18334 datalog$fn__18233$join__18334 = join;
        join = null;
        IPersistentVector iPersistentVector3 = probelists;
        probelists = null;
        ((IFn)const__30.getRawRoot()).invoke((Object)new datalog$fn__18233$fn__18337((Object)datalog$fn__18233$join__18334, ret), (Object)iPersistentVector3);
        Set set = ret;
        ret = null;
        return set;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return datalog$fn__18233.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}


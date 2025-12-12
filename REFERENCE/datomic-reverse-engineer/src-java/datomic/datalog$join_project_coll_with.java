/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LOO
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$join_project_coll_with$fn__18131;
import datomic.datalog$join_project_coll_with$proc__18145;
import datomic.datalog$join_project_coll_with$project__18141;
import datomic.datalog.IJoin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class datalog$join_project_coll_with
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__10;
    public static final Object const__11;
    public static final Var const__13;
    public static final Keyword const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Var const__22;
    public static final Var const__23;
    public static final Object const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final Var const__27;
    public static final Var const__30;
    public static final Var const__31;
    public static final Var const__32;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object xs, Object ys, Object join_map, Object project_map_x, Object project_map_y, Object predctor) {
        datalog$join_project_coll_with$proc__18145 proc;
        HashMap hashMap;
        Object object;
        Object and__5236__auto__18154;
        Object object2;
        Object ks;
        Object object3;
        Object and__5236__auto__18153;
        Set set;
        if ((long)RT.count(xs) < (long)RT.count((Object)ys)) {
            Object object4 = ys;
            ys = null;
            Object object5 = object4;
            if (Util.classOf((Object)object4) != __cached_class__0) {
                if (object5 instanceof IJoin) {
                    Set set2 = xs;
                    xs = null;
                    Object object7 = ((IFn)const__4.getRawRoot()).invoke(join_map);
                    object7 = join_map;
                    join_map = null;
                    Object object8 = project_map_y;
                    project_map_y = null;
                    Object object9 = project_map_x;
                    project_map_x = null;
                    Object object10 = predctor;
                    predctor = null;
                    set = ((IJoin)object5).join_project_with(set2, ((IFn)const__3.getRawRoot()).invoke(object6, ((IFn)const__5.getRawRoot()).invoke(object7)), object8, object9, object10);
                    return set;
                }
                object5 = object5;
                __cached_class__0 = Util.classOf((Object)object5);
            }
            Set set3 = xs;
            xs = null;
            Object object12 = ((IFn)const__4.getRawRoot()).invoke(join_map);
            object12 = join_map;
            join_map = null;
            Object object13 = project_map_y;
            project_map_y = null;
            Object object14 = project_map_x;
            project_map_x = null;
            Object object15 = predctor;
            predctor = null;
            set = const__2.getRawRoot().invoke(object5, set3, ((IFn)const__3.getRawRoot()).invoke(object11, ((IFn)const__5.getRawRoot()).invoke(object12)), object13, object14, object15);
            return set;
        }
        Object object16 = and__5236__auto__18153 = ((IFn)const__6.getRawRoot()).invoke(join_map);
        if (object16 != null && object16 != Boolean.FALSE) {
            Object and__5236__auto__18152;
            Object object17 = and__5236__auto__18152 = ((IFn)const__6.getRawRoot()).invoke(project_map_y);
            if (object17 != null && object17 != Boolean.FALSE) {
                boolean and__5236__auto__18149;
                boolean and__5236__auto__18150;
                boolean and__5236__auto__18151 = Util.equiv((long)RT.count((Object)project_map_x), (Object)(((IFn)const__10.getRawRoot()).invoke(xs) instanceof Map.Entry ? const__11 : Integer.valueOf(RT.count((Object)((IFn)const__10.getRawRoot()).invoke(xs)))));
                object3 = and__5236__auto__18151 ? ((and__5236__auto__18150 = Util.equiv((Object)((IFn)const__5.getRawRoot()).invoke(project_map_x), (Object)((IFn)const__4.getRawRoot()).invoke(project_map_x))) ? ((and__5236__auto__18149 = Util.identical((Object)const__13.getRawRoot(), (Object)predctor)) ? (xs instanceof Set ? Boolean.TRUE : Boolean.FALSE) : (and__5236__auto__18149 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__18150 ? Boolean.TRUE : Boolean.FALSE)) : (and__5236__auto__18151 ? Boolean.TRUE : Boolean.FALSE);
            } else {
                object3 = and__5236__auto__18152;
                and__5236__auto__18152 = null;
            }
        } else {
            object3 = and__5236__auto__18153;
            and__5236__auto__18153 = null;
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            set = xs;
            return set;
        }
        Keyword keyword = const__15;
        if (keyword == null) return null;
        if (keyword == Boolean.FALSE) return null;
        Object px_from = ((IFn)const__16.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(project_map_x));
        Object object18 = project_map_x;
        project_map_x = null;
        Object px_to = ((IFn)const__16.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object18));
        Object py_from = ((IFn)const__16.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(project_map_y));
        Object object19 = project_map_y;
        project_map_y = null;
        Object py_to = ((IFn)const__16.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object19));
        int proj_count = RT.count((Object)((IFn)const__17.getRawRoot()).invoke(((IFn)const__18.getRawRoot()).invoke(px_to), py_to));
        Object object20 = ks = ((IFn)const__19.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(join_map));
        if (object20 != null && object20 != Boolean.FALSE) {
            Object object21 = ks;
            ks = null;
            object2 = Numbers.unchecked_inc((Object)((IFn)const__22.getRawRoot()).invoke(const__23.getRawRoot(), object21));
        } else {
            object2 = const__24;
        }
        Object[] bindings = RT.object_array((Object)object2);
        Object object22 = join_map;
        join_map = null;
        ((IFn)new datalog$join_project_coll_with$fn__18131(object22, bindings)).invoke();
        Object match_QMARK_ = ((IFn)const__25.getRawRoot()).invoke((Object)bindings);
        Object hashx = ((IFn)const__26.getRawRoot()).invoke((Object)bindings);
        Object hashy = ((IFn)const__27.getRawRoot()).invoke((Object)bindings);
        Object object23 = px_from;
        px_from = null;
        Object object24 = py_to;
        py_to = null;
        Object object25 = py_from;
        py_from = null;
        Object object26 = px_to;
        px_to = null;
        datalog$join_project_coll_with$project__18141 project2 = new datalog$join_project_coll_with$project__18141(object23, object24, object25, proj_count, object26);
        Set ret = Collections.newSetFromMap(new ConcurrentHashMap());
        Object[] objectArray = bindings;
        bindings = null;
        Object object27 = and__5236__auto__18154 = ((IFn)const__19.getRawRoot()).invoke((Object)objectArray);
        if (object27 != null && object27 != Boolean.FALSE) {
            object = Numbers.gt((long)RT.count((Object)ys), (long)10L) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5236__auto__18154;
            and__5236__auto__18154 = null;
        }
        if (object == null || object == Boolean.FALSE) {
            hashMap = null;
        } else {
            HashMap ret2 = new HashMap();
            Object yiter = ((IFn)const__30.getRawRoot()).invoke(ys);
            while (((Iterator)yiter).hasNext()) {
                Boolean bl;
                Object vs;
                Object object28;
                Object or__5238__auto__18155;
                Object y = ((Iterator)yiter).next();
                Object h = ((IFn)hashy).invoke(y);
                Object v = or__5238__auto__18155 = ret2.get(h);
                if (v != null && v != Boolean.FALSE) {
                    object28 = or__5238__auto__18155;
                    or__5238__auto__18155 = null;
                } else {
                    ArrayList vs2 = new ArrayList(RT.uncheckedIntCast((long)2L));
                    Object object29 = h;
                    h = null;
                    ret2.put(object29, vs2);
                    object28 = vs2;
                    vs2 = null;
                }
                Object v2 = vs = object28;
                vs = null;
                Object e = y;
                y = null;
                if (((ArrayList)v2).add(e)) {
                    bl = Boolean.TRUE;
                    continue;
                }
                bl = Boolean.FALSE;
            }
            hashMap = ret2;
            ret2 = null;
        }
        HashMap ht = hashMap;
        long PART = 10L;
        Object object30 = ys;
        ys = null;
        Object object31 = match_QMARK_;
        match_QMARK_ = null;
        HashMap hashMap2 = ht;
        ht = null;
        datalog$join_project_coll_with$project__18141 datalog$join_project_coll_with$project__18141 = project2;
        project2 = null;
        Object object32 = hashx;
        hashx = null;
        Object object33 = predctor;
        predctor = null;
        datalog$join_project_coll_with$proc__18145 datalog$join_project_coll_with$proc__18145 = proc = new datalog$join_project_coll_with$proc__18145(object30, object31, hashMap2, ret, (Object)datalog$join_project_coll_with$project__18141, object32, object33);
        proc = null;
        Set set4 = xs;
        xs = null;
        ((IFn)const__31.getRawRoot()).invoke((Object)datalog$join_project_coll_with$proc__18145, ((IFn.LOO)const__32.getRawRoot()).invokePrim(PART, set4));
        set = ret;
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
        return datalog$join_project_coll_with.invokeStatic(object7, object8, object9, object10, object11, object12);
    }

    static {
        const__2 = RT.var((String)"datomic.datalog", (String)"join-project-with");
        const__3 = RT.var((String)"clojure.core", (String)"zipmap");
        const__4 = RT.var((String)"clojure.core", (String)"vals");
        const__5 = RT.var((String)"clojure.core", (String)"keys");
        const__6 = RT.var((String)"clojure.core", (String)"empty?");
        const__10 = RT.var((String)"clojure.core", (String)"first");
        const__11 = 2L;
        const__13 = RT.var((String)"datomic.datalog", (String)"truep");
        const__15 = RT.keyword(null, (String)"else");
        const__16 = RT.var((String)"clojure.core", (String)"to-array");
        const__17 = RT.var((String)"clojure.core", (String)"into");
        const__18 = RT.var((String)"clojure.core", (String)"set");
        const__19 = RT.var((String)"clojure.core", (String)"seq");
        const__22 = RT.var((String)"clojure.core", (String)"apply");
        const__23 = RT.var((String)"clojure.core", (String)"max");
        const__24 = 0L;
        const__25 = RT.var((String)"datomic.datalog", (String)"matchf");
        const__26 = RT.var((String)"datomic.datalog", (String)"hashxf");
        const__27 = RT.var((String)"datomic.datalog", (String)"hashyf");
        const__30 = RT.var((String)"datomic.datalog", (String)"iterator");
        const__31 = RT.var((String)"datomic.datalog", (String)"qmapv");
        const__32 = RT.var((String)"datomic.datalog", (String)"partv");
    }
}


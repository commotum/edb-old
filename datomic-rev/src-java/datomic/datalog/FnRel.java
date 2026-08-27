/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LOO
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Indexed
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.datalog;

import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Indexed;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog.FnRel$fn__18356;
import datomic.datalog.FnRel$proc__18366;
import datomic.datalog.FnRel$project__18348;
import datomic.datalog.IJoin;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashSet;

public final class FnRel
implements IJoin,
IType {
    public final Object db;
    public final Object f;
    public final Object arity;
    public final Object src_QMARK_;
    public final Object consts;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__6;
    public static final Var const__7;
    public static final Var const__8;
    public static final Var const__11;
    public static final Var const__12;
    public static final Object const__13;
    public static final Var const__14;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__20;
    public static final Var const__21;
    public static final Var const__22;
    public static final Var const__24;
    public static final Var const__25;

    public FnRel(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.db = object;
        this.f = object2;
        this.arity = object3;
        this.src_QMARK_ = object4;
        this.consts = object5;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"arity"), (Object)Symbol.intern(null, (String)"src?"), (Object)Symbol.intern(null, (String)"consts"));
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object join_project_with(Object ys, Object join_map, Object project_map_x, Object project_map_y, Object predctor) {
        FnRel$proc__18366 proc;
        Object object;
        Object ks;
        HashSet hashSet;
        if (Numbers.isZero((Object)this_.arity)) {
            Object object2 = this_.src_QMARK_;
            Object object3 = object2 != null && object2 != Boolean.FALSE ? ((IFn)this_.f).invoke(this_.db) : ((IFn)this_.f).invoke();
            if (Util.classOf((Object)object3) != __cached_class__1) {
                if (object3 instanceof IJoin) {
                    Object object4 = ys;
                    ys = null;
                    Object object5 = join_map;
                    join_map = null;
                    Object object6 = project_map_x;
                    project_map_x = null;
                    Object object7 = project_map_y;
                    project_map_y = null;
                    Object object8 = predctor;
                    predctor = null;
                    hashSet = ((IJoin)object3).join_project_with(object4, object5, object6, object7, object8);
                    return hashSet;
                }
                object3 = object3;
                __cached_class__1 = Util.classOf((Object)object3);
            }
            Object object9 = ys;
            ys = null;
            Object object10 = join_map;
            join_map = null;
            Object object11 = project_map_x;
            project_map_x = null;
            Object object12 = project_map_y;
            project_map_y = null;
            Object object13 = predctor;
            predctor = null;
            FnRel this_ = null;
            hashSet = const__0.getRawRoot().invoke(object3, object9, object10, object11, object12, object13);
            return hashSet;
        }
        HashSet rel = new HashSet();
        Object px_from = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(project_map_x));
        Object object14 = project_map_x;
        project_map_x = null;
        Object px_to = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object14));
        Object py_from = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(project_map_y));
        Object object15 = project_map_y;
        project_map_y = null;
        Object py_to = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object15));
        int proj_count = RT.count((Object)((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(px_to), py_to));
        Object object16 = px_to;
        px_to = null;
        Object object17 = px_from;
        px_from = null;
        Object object18 = py_from;
        py_from = null;
        Object object19 = py_to;
        py_to = null;
        FnRel$project__18348 project2 = new FnRel$project__18348(object16, object17, object18, proj_count, object19);
        Object object20 = ks = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(join_map));
        if (object20 != null && object20 != Boolean.FALSE) {
            Object object21 = ks;
            ks = null;
            object = Numbers.unchecked_inc((Object)((IFn)const__11.getRawRoot()).invoke(const__12.getRawRoot(), object21));
        } else {
            object = const__13;
        }
        Object[] bindings = RT.object_array((Object)object);
        ((IFn)new FnRel$fn__18356(join_map, bindings)).invoke();
        Object[] objectArray = bindings;
        bindings = null;
        Object match_QMARK_ = ((IFn)const__14.getRawRoot()).invoke((Object)objectArray);
        long PART = 100L;
        Object object22 = predctor;
        predctor = null;
        FnRel$project__18348 fnRel$project__18348 = project2;
        project2 = null;
        Object object23 = join_map;
        join_map = null;
        Object object24 = match_QMARK_;
        match_QMARK_ = null;
        FnRel$proc__18366 fnRel$proc__18366 = proc = new FnRel$proc__18366(object22, this_.consts, (Object)fnRel$project__18348, this_.f, PART, this_.db, object23, this_.arity, object24, this_.src_QMARK_);
        proc = null;
        Object object25 = ys;
        ys = null;
        Object seq_18369 = ((IFn)const__8.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke((Object)fnRel$proc__18366, ((IFn.LOO)const__17.getRawRoot()).invokePrim(PART, object25)));
        Object chunk_18370 = null;
        long count_18371 = 0L;
        long i_18372 = 0L;
        while (true) {
            Object part2;
            Object temp__5457__auto__18375;
            if (i_18372 < count_18371) {
                Object part3;
                Object object26 = part3 = ((Indexed)chunk_18370).nth(RT.uncheckedIntCast((long)i_18372));
                part3 = null;
                Boolean bl = ((AbstractCollection)rel).addAll((Collection)object26) ? Boolean.TRUE : Boolean.FALSE;
                Object object27 = seq_18369;
                seq_18369 = null;
                Object object28 = chunk_18370;
                chunk_18370 = null;
                ++i_18372;
                chunk_18370 = object28;
                seq_18369 = object27;
                continue;
            }
            Object object29 = seq_18369;
            seq_18369 = null;
            Object object30 = temp__5457__auto__18375 = ((IFn)const__8.getRawRoot()).invoke(object29);
            if (object30 == null || object30 == Boolean.FALSE) break;
            Object object31 = temp__5457__auto__18375;
            temp__5457__auto__18375 = null;
            Object seq_183692 = object31;
            Object object32 = ((IFn)const__20.getRawRoot()).invoke(seq_183692);
            if (object32 != null && object32 != Boolean.FALSE) {
                Object c__5719__auto__18374 = ((IFn)const__21.getRawRoot()).invoke(seq_183692);
                Object object33 = seq_183692;
                seq_183692 = null;
                Object object34 = c__5719__auto__18374;
                Object object35 = c__5719__auto__18374;
                c__5719__auto__18374 = null;
                i_18372 = (int)0L;
                count_18371 = RT.count((Object)object35);
                chunk_18370 = object34;
                seq_18369 = ((IFn)const__22.getRawRoot()).invoke(object33);
                continue;
            }
            Object object36 = part2 = ((IFn)const__24.getRawRoot()).invoke(seq_183692);
            part2 = null;
            Boolean bl = ((AbstractCollection)rel).addAll((Collection)object36) ? Boolean.TRUE : Boolean.FALSE;
            Object object37 = seq_183692;
            seq_183692 = null;
            i_18372 = 0L;
            count_18371 = 0L;
            chunk_18370 = null;
            seq_18369 = ((IFn)const__25.getRawRoot()).invoke(object37);
        }
        hashSet = rel;
        return hashSet;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object join_project(Object ys, Object join_map, Object project_map_x, Object project_map_y, Object predctor) {
        Object object;
        FnRel fnRel = this_;
        if (Util.classOf((Object)fnRel) != __cached_class__0) {
            if (fnRel instanceof IJoin) {
                Object object2 = ys;
                ys = null;
                Object object3 = join_map;
                join_map = null;
                Object object4 = project_map_x;
                project_map_x = null;
                Object object5 = project_map_y;
                project_map_y = null;
                Object object6 = predctor;
                predctor = null;
                object = ((IJoin)fnRel).join_project_with(object2, object3, object4, object5, object6);
                return object;
            }
            fnRel = fnRel;
            __cached_class__0 = Util.classOf((Object)fnRel);
        }
        Object object7 = ys;
        ys = null;
        Object object8 = join_map;
        join_map = null;
        Object object9 = project_map_x;
        project_map_x = null;
        Object object10 = project_map_y;
        project_map_y = null;
        Object object11 = predctor;
        predctor = null;
        FnRel this_ = null;
        object = const__0.getRawRoot().invoke((Object)fnRel, object7, object8, object9, object10, object11);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.datalog", (String)"join-project-with");
        const__2 = RT.var((String)"clojure.core", (String)"to-array");
        const__3 = RT.var((String)"clojure.core", (String)"keys");
        const__4 = RT.var((String)"clojure.core", (String)"vals");
        const__6 = RT.var((String)"clojure.core", (String)"into");
        const__7 = RT.var((String)"clojure.core", (String)"set");
        const__8 = RT.var((String)"clojure.core", (String)"seq");
        const__11 = RT.var((String)"clojure.core", (String)"apply");
        const__12 = RT.var((String)"clojure.core", (String)"max");
        const__13 = 0L;
        const__14 = RT.var((String)"datomic.datalog", (String)"matchf");
        const__16 = RT.var((String)"datomic.datalog", (String)"qmapv");
        const__17 = RT.var((String)"datomic.datalog", (String)"partv");
        const__20 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__21 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__22 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__24 = RT.var((String)"clojure.core", (String)"first");
        const__25 = RT.var((String)"clojure.core", (String)"next");
    }
}


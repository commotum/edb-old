/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
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
import datomic.datalog.IJoin;
import java.util.HashSet;
import java.util.Iterator;

public final class PredRel
implements IJoin,
IType {
    public final Object db;
    public final Object f;
    public final Object arity;
    public final Object src_QMARK_;
    public final Object consts;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__2;
    public static final Var const__4;
    public static final Var const__12;
    public static final Var const__13;
    public static final Var const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__19;
    public static final Var const__21;
    public static final Var const__23;

    public PredRel(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.db = object;
        this.f = object2;
        this.arity = object3;
        this.src_QMARK_ = object4;
        this.consts = object5;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"f"), (Object)Symbol.intern(null, (String)"arity"), (Object)Symbol.intern(null, (String)"src?"), (Object)Symbol.intern(null, (String)"consts"));
    }

    public Object join_project_with(Object ys, Object join_map, Object project_map_x, Object project_map_y, Object _) {
        HashSet<Object> rel = new HashSet<Object>();
        Object[] args = RT.object_array((Object)this.arity);
        Object object = ys;
        ys = null;
        Object yiter = ((IFn)const__2.getRawRoot()).invoke(object);
        int n = RT.count((Object)project_map_y);
        Object[] invpmy = RT.object_array((Object)n);
        Object object2 = project_map_y;
        project_map_y = null;
        Object seq_18397 = ((IFn)const__4.getRawRoot()).invoke(object2);
        Object chunk_18398 = null;
        long count_18399 = 0L;
        long i_18400 = 0L;
        while (true) {
            Object i;
            Object temp__5457__auto__18409;
            if (i_18400 < count_18399) {
                Object i2;
                Object vec__18401 = ((Indexed)chunk_18398).nth(RT.uncheckedIntCast((long)i_18400));
                Object y = RT.nth((Object)vec__18401, (int)RT.uncheckedIntCast((long)0L), null);
                Object object3 = vec__18401;
                vec__18401 = null;
                Object object4 = i2 = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
                i2 = null;
                Object object5 = y;
                y = null;
                RT.aset((Object[])invpmy, (int)RT.uncheckedIntCast((Object)object4), (Object)object5);
                Object object6 = seq_18397;
                seq_18397 = null;
                Object object7 = chunk_18398;
                chunk_18398 = null;
                ++i_18400;
                chunk_18398 = object7;
                seq_18397 = object6;
                continue;
            }
            Object object8 = seq_18397;
            seq_18397 = null;
            Object object9 = temp__5457__auto__18409 = ((IFn)const__4.getRawRoot()).invoke(object8);
            if (object9 == null || object9 == Boolean.FALSE) break;
            Object object10 = temp__5457__auto__18409;
            temp__5457__auto__18409 = null;
            Object seq_183972 = object10;
            Object object11 = ((IFn)const__12.getRawRoot()).invoke(seq_183972);
            if (object11 != null && object11 != Boolean.FALSE) {
                Object c__5719__auto__18408 = ((IFn)const__13.getRawRoot()).invoke(seq_183972);
                Object object12 = seq_183972;
                seq_183972 = null;
                Object object13 = c__5719__auto__18408;
                Object object14 = c__5719__auto__18408;
                c__5719__auto__18408 = null;
                i_18400 = (int)0L;
                count_18399 = RT.count((Object)object14);
                chunk_18398 = object13;
                seq_18397 = ((IFn)const__14.getRawRoot()).invoke(object12);
                continue;
            }
            Object vec__18404 = ((IFn)const__15.getRawRoot()).invoke(seq_183972);
            Object y = RT.nth((Object)vec__18404, (int)RT.uncheckedIntCast((long)0L), null);
            Object object15 = vec__18404;
            vec__18404 = null;
            Object object16 = i = RT.nth((Object)object15, (int)RT.uncheckedIntCast((long)1L), null);
            i = null;
            Object object17 = y;
            y = null;
            RT.aset((Object[])invpmy, (int)RT.uncheckedIntCast((Object)object16), (Object)object17);
            Object object18 = seq_183972;
            seq_183972 = null;
            i_18400 = 0L;
            count_18399 = 0L;
            chunk_18398 = null;
            seq_18397 = ((IFn)const__16.getRawRoot()).invoke(object18);
        }
        while (((Iterator)yiter).hasNext()) {
            ((IFn)const__17.getRawRoot()).invoke();
            Object y = ((Iterator)yiter).next();
            long n__5742__auto__18410 = RT.longCast((Object)this.arity);
            for (long i = 0L; i < n__5742__auto__18410; ++i) {
                Object object19 = ((IFn)const__19.getRawRoot()).invoke((Object)(Util.identical((Object)((IFn)this.consts).invoke((Object)Numbers.num((long)i)), null) ? Boolean.TRUE : Boolean.FALSE));
                RT.aset((Object[])args, (int)((int)i), (Object)(object19 != null && object19 != Boolean.FALSE ? ((IFn)this.consts).invoke((Object)Numbers.num((long)i)) : RT.nth(y, (int)RT.uncheckedIntCast((Object)((Number)((IFn)join_map).invoke((Object)Numbers.num((long)i)))))));
            }
            Object object20 = this.src_QMARK_;
            Object object21 = object20 != null && object20 != Boolean.FALSE ? ((IFn)const__21.getRawRoot()).invoke(this.f, this.db, (Object)args) : ((IFn)const__21.getRawRoot()).invoke(this.f, (Object)args);
            if (object21 == null || object21 == Boolean.FALSE) continue;
            Object[] tos = RT.object_array((Object)n);
            long n__5742__auto__18411 = n;
            for (long i = 0L; i < n__5742__auto__18411; ++i) {
                RT.aset((Object[])tos, (int)((int)i), (Object)RT.nth(y, (int)RT.uncheckedIntCast((Object)((Number)RT.aget((Object[])invpmy, (int)((int)i))))));
            }
            Object[] objectArray = tos;
            tos = null;
            Boolean bl = rel.add(((IFn)const__23.getRawRoot()).invoke((Object)objectArray)) ? Boolean.TRUE : Boolean.FALSE;
        }
        HashSet<Object> hashSet = rel;
        rel = null;
        return hashSet;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object join_project(Object ys, Object join_map, Object project_map_x, Object project_map_y, Object predctor) {
        Object object;
        PredRel predRel = this_;
        if (Util.classOf((Object)predRel) != __cached_class__0) {
            if (predRel instanceof IJoin) {
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
                object = ((IJoin)predRel).join_project_with(object2, object3, object4, object5, object6);
                return object;
            }
            predRel = predRel;
            __cached_class__0 = Util.classOf((Object)predRel);
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
        PredRel this_ = null;
        object = const__0.getRawRoot().invoke((Object)predRel, object7, object8, object9, object10, object11);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.datalog", (String)"join-project-with");
        const__2 = RT.var((String)"datomic.datalog", (String)"iterator");
        const__4 = RT.var((String)"clojure.core", (String)"seq");
        const__12 = RT.var((String)"clojure.core", (String)"chunked-seq?");
        const__13 = RT.var((String)"clojure.core", (String)"chunk-first");
        const__14 = RT.var((String)"clojure.core", (String)"chunk-rest");
        const__15 = RT.var((String)"clojure.core", (String)"first");
        const__16 = RT.var((String)"clojure.core", (String)"next");
        const__17 = RT.var((String)"datomic.datalog", (String)"maybe-cancel");
        const__19 = RT.var((String)"clojure.core", (String)"not");
        const__21 = RT.var((String)"clojure.core", (String)"apply");
        const__23 = RT.var((String)"datomic.datalog", (String)"tuple");
    }
}


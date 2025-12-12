/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$group_rel$agg__19454;
import datomic.query$group_rel$fn__19450;
import datomic.query$group_rel$fn__19461;
import datomic.query$group_rel$reify__19452;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public final class query$group_rel
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"filterv");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"mapv");
    public static final AFn const__9 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 626, RT.keyword(null, (String)"column"), 17});
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"vec");

    public static Object invokeStatic(Object fv, Object rel) {
        Object object;
        Object grp_idxs = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), fv);
        if ((long)RT.count((Object)fv) == (long)RT.count((Object)grp_idxs)) {
            Object object2 = fv;
            fv = null;
            Object object3 = rel;
            rel = null;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)new query$group_rel$fn__19450(object2), object3);
        } else {
            Object object4 = grp_idxs;
            grp_idxs = null;
            IObj cmp = ((IObj)new query$group_rel$reify__19452(null, object4)).withMeta((IPersistentMap)const__9);
            Object object5 = rel;
            rel = null;
            ArrayList srel = new ArrayList((Collection)object5);
            Collections.sort(srel, (Comparator)cmp);
            query$group_rel$agg__19454 agg2 = new query$group_rel$agg__19454(srel);
            Object ret = PersistentVector.EMPTY;
            long r = 0L;
            while (true) {
                if (r == (long)srel.size()) {
                    object = ret;
                    ret = null;
                    break;
                }
                Object row = srel.get(RT.intCast((long)r));
                long nextr = RT.longCast((Object)((IFn)new query$group_rel$fn__19461(r, srel, cmp, row)).invoke());
                Object[] nextrow = RT.object_array((Object)RT.count((Object)fv));
                long cnt = Numbers.minus((long)nextr, (long)r);
                long n__5742__auto__19467 = RT.count((Object)fv);
                for (long i = 0L; i < n__5742__auto__19467; ++i) {
                    Object object6;
                    Object fve = RT.nth((Object)fv, (int)RT.intCast((long)i));
                    int n = RT.intCast((long)i);
                    Object object7 = ((IFn)const__1.getRawRoot()).invoke(fve);
                    if (object7 != null && object7 != Boolean.FALSE) {
                        Object object8 = fve;
                        fve = null;
                        object6 = RT.nth(row, (int)RT.intCast((Object)((Number)object8)));
                    } else {
                        Object f;
                        Object object9 = fve;
                        fve = null;
                        Object vec__19463 = object9;
                        Object idx = RT.nth((Object)vec__19463, (int)RT.intCast((long)0L), null);
                        Object object10 = vec__19463;
                        vec__19463 = null;
                        Object object11 = f = RT.nth((Object)object10, (int)RT.intCast((long)1L), null);
                        f = null;
                        Object object12 = idx;
                        idx = null;
                        object6 = ((IFn)object11).invoke(((IFn)agg2).invoke(object12, (Object)Numbers.num((long)r), (Object)Numbers.num((long)cnt)));
                    }
                    RT.aset((Object[])nextrow, (int)n, (Object)object6);
                }
                PersistentVector persistentVector = ret;
                ret = null;
                Object[] objectArray = nextrow;
                nextrow = null;
                r = nextr;
                ret = ((IFn)const__20.getRawRoot()).invoke((Object)persistentVector, ((IFn)const__21.getRawRoot()).invoke((Object)objectArray));
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$group_rel.invokeStatic(object3, object4);
    }
}


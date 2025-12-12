/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$eval_rule$fn__18831;
import datomic.datalog$eval_rule$fn__18833;
import datomic.db.IDb;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class datalog$eval_rule
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"keep-indexed");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"not");
    public static final AFn const__11 = (AFn)Symbol.intern(null, (String)"$");
    public static final Var const__12 = RT.var((String)"datomic.datalog", (String)"push-preds");
    public static final Var const__13 = RT.var((String)"datomic.datalog", (String)"remap-bounds");

    public static Object invokeStatic(Object db2, Object prog, Object oprog, Object p__18820, Object p__18821, Object inrel, Object sched_fn, Object src, Object ans, Object ins, Object top_bounds, Object nested_bounds) {
        Object object;
        Object multi_QMARK_;
        Object object2;
        Object and__5236__auto__18843;
        Object vec__18828;
        Object object3 = p__18820;
        p__18820 = null;
        Object vec__18822 = object3;
        Object seq__18823 = ((IFn)const__0.getRawRoot()).invoke(vec__18822);
        Object first__18824 = ((IFn)const__1.getRawRoot()).invoke(seq__18823);
        Object object4 = seq__18823;
        seq__18823 = null;
        Object seq__188232 = ((IFn)const__2.getRawRoot()).invoke(object4);
        Object object5 = first__18824;
        first__18824 = null;
        Object head = object5;
        seq__188232 = null;
        Object object6 = vec__18822;
        vec__18822 = null;
        Object rule = object6;
        Object object7 = p__18821;
        p__18821 = null;
        Object vec__18825 = object7;
        Object pred2 = RT.nth((Object)vec__18825, (int)RT.uncheckedIntCast((long)0L), null);
        Object adorn = RT.nth((Object)vec__18825, (int)RT.uncheckedIntCast((long)1L), null);
        vec__18825 = null;
        Object object8 = vec__18828 = head;
        vec__18828 = null;
        Object seq__18829 = ((IFn)const__0.getRawRoot()).invoke(object8);
        Object first__18830 = ((IFn)const__1.getRawRoot()).invoke(seq__18829);
        Object object9 = seq__18829;
        seq__18829 = null;
        Object seq__188292 = ((IFn)const__2.getRawRoot()).invoke(object9);
        first__18830 = null;
        Object object10 = seq__188292;
        seq__188292 = null;
        Object hargs = object10;
        Object object11 = pred2;
        pred2 = null;
        IPersistentVector aresk = Tuple.create((Object)src, (Object)object11);
        Object object12 = adorn;
        adorn = null;
        Object inbinds = ((IFn)const__6.getRawRoot()).invoke((Object)new datalog$eval_rule$fn__18831(object12), hargs);
        Object object13 = and__5236__auto__18843 = ((IFn)const__7.getRawRoot()).invoke(db2);
        if (object13 != null && object13 != Boolean.FALSE) {
            object2 = ((IFn)const__8.getRawRoot()).invoke((Object)(db2 instanceof IDb ? Boolean.TRUE : Boolean.FALSE));
        } else {
            object2 = and__5236__auto__18843;
            and__5236__auto__18843 = null;
        }
        Object object14 = multi_QMARK_ = object2;
        Object srcs = object14 != null && object14 != Boolean.FALSE ? db2 : RT.mapUniqueKeys((Object[])new Object[]{const__11, db2});
        Object object15 = rule;
        rule = null;
        Object cbs = ((IFn)const__12.getRawRoot()).invoke(srcs, ((IFn)sched_fn).invoke(srcs, prog, object15, inbinds));
        Object object16 = nested_bounds;
        if (object16 != null && object16 != Boolean.FALSE) {
            Object object17 = nested_bounds;
            nested_bounds = null;
            object = ((IFn)const__13.getRawRoot()).invoke(object17, hargs);
        } else {
            object = top_bounds;
            top_bounds = null;
        }
        Object top_bounds2 = object;
        Object object18 = db2;
        db2 = null;
        Object object19 = oprog;
        oprog = null;
        Object object20 = top_bounds2;
        top_bounds2 = null;
        Object object21 = inbinds;
        inbinds = null;
        Object object22 = srcs;
        srcs = null;
        Object object23 = hargs;
        hargs = null;
        Object object24 = ins;
        ins = null;
        Object object25 = head;
        head = null;
        Object object26 = src;
        src = null;
        Object object27 = multi_QMARK_;
        multi_QMARK_ = null;
        Object object28 = prog;
        prog = null;
        Object object29 = inrel;
        inrel = null;
        Object object30 = cbs;
        cbs = null;
        Object object31 = sched_fn;
        sched_fn = null;
        Object res = ((IFn)new datalog$eval_rule$fn__18833(ans, object18, object19, object20, object21, object22, object23, object24, object25, object26, object27, object28, object29, object30, object31)).invoke();
        Object anspred = RT.get((Object)ans, (Object)aresk, new HashSet());
        Object object32 = res;
        res = null;
        Boolean bl = ((Set)anspred).addAll((Collection)object32) ? Boolean.TRUE : Boolean.FALSE;
        Object object33 = ans;
        ans = null;
        IPersistentVector iPersistentVector = aresk;
        aresk = null;
        Object object34 = anspred;
        anspred = null;
        ((Map)object33).put(iPersistentVector, object34);
        return null;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11, Object object12) {
        Object object13 = object;
        object = null;
        Object object14 = object2;
        object2 = null;
        Object object15 = object3;
        object3 = null;
        Object object16 = object4;
        object4 = null;
        Object object17 = object5;
        object5 = null;
        Object object18 = object6;
        object6 = null;
        Object object19 = object7;
        object7 = null;
        Object object20 = object8;
        object8 = null;
        Object object21 = object9;
        object9 = null;
        Object object22 = object10;
        object10 = null;
        Object object23 = object11;
        object11 = null;
        Object object24 = object12;
        object12 = null;
        return datalog$eval_rule.invokeStatic(object13, object14, object15, object16, object17, object18, object19, object20, object21, object22, object23, object24);
    }
}


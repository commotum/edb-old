/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class datalog$eval_query
extends AFunction {
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"every?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__9 = RT.var((String)"datomic.datalog", (String)"eval-rule");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object db2, Object prog, Object oprog, Object p__18844, Object input, Object sched_fn, Object src, Object ans, Object ins, Object top_bounds, Object nested_bounds) {
        block5: {
            Object object;
            Object or__5238__auto__18853;
            Object object2 = p__18844;
            p__18844 = null;
            Object vec__18845 = object2;
            Object pred2 = RT.nth((Object)vec__18845, (int)RT.uncheckedIntCast((long)0L), null);
            Object adorn = RT.nth((Object)vec__18845, (int)RT.uncheckedIntCast((long)1L), null);
            Object object3 = vec__18845;
            vec__18845 = null;
            Object apred = object3;
            IPersistentVector iresk = Tuple.create((Object)src, (Object)apred);
            Object inpred = RT.get((Object)ins, (Object)iresk, new HashSet());
            Object object4 = input;
            input = null;
            HashSet input2 = new HashSet((Collection)object4);
            Boolean bl = ((AbstractSet)input2).removeAll((Collection)inpred) ? Boolean.TRUE : Boolean.FALSE;
            Object object5 = or__5238__auto__18853 = ((IFn)const__4.getRawRoot()).invoke((Object)(input2.isEmpty() ? Boolean.TRUE : Boolean.FALSE));
            if (object5 != null && object5 != Boolean.FALSE) {
                object = or__5238__auto__18853;
                or__5238__auto__18853 = null;
            } else {
                Object object6 = adorn;
                adorn = null;
                object = ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), object6);
            }
            if (object == null || object == Boolean.FALSE) break block5;
            Object object7 = pred2;
            pred2 = null;
            Object rules = RT.get((Object)prog, (Object)object7);
            Boolean bl2 = ((Set)inpred).addAll(input2) ? Boolean.TRUE : Boolean.FALSE;
            IPersistentVector iPersistentVector = iresk;
            iresk = null;
            Object object8 = inpred;
            inpred = null;
            ((Map)ins).put(iPersistentVector, object8);
            Object object9 = rules;
            rules = null;
            Object seq_18848 = ((IFn)const__7.getRawRoot()).invoke(object9);
            Object chunk_18849 = null;
            long count_18850 = 0L;
            long i_18851 = 0L;
            while (true) {
                Object rule;
                Object temp__5457__auto__18855;
                if (i_18851 < count_18850) {
                    Object rule2;
                    Object object10 = rule2 = ((Indexed)chunk_18849).nth(RT.uncheckedIntCast((long)i_18851));
                    rule2 = null;
                    ((IFn)const__9.getRawRoot()).invoke(db2, prog, oprog, object10, apred, input2, sched_fn, src, ans, ins, top_bounds, nested_bounds);
                    Object object11 = seq_18848;
                    seq_18848 = null;
                    Object object12 = chunk_18849;
                    chunk_18849 = null;
                    ++i_18851;
                    chunk_18849 = object12;
                    seq_18848 = object11;
                    continue;
                }
                Object object13 = seq_18848;
                seq_18848 = null;
                Object object14 = temp__5457__auto__18855 = ((IFn)const__7.getRawRoot()).invoke(object13);
                if (object14 == null || object14 == Boolean.FALSE) break;
                Object object15 = temp__5457__auto__18855;
                temp__5457__auto__18855 = null;
                Object seq_188482 = object15;
                Object object16 = ((IFn)const__11.getRawRoot()).invoke(seq_188482);
                if (object16 != null && object16 != Boolean.FALSE) {
                    Object c__5719__auto__18854 = ((IFn)const__12.getRawRoot()).invoke(seq_188482);
                    Object object17 = seq_188482;
                    seq_188482 = null;
                    Object object18 = c__5719__auto__18854;
                    Object object19 = c__5719__auto__18854;
                    c__5719__auto__18854 = null;
                    i_18851 = (int)0L;
                    count_18850 = RT.count((Object)object19);
                    chunk_18849 = object18;
                    seq_18848 = ((IFn)const__13.getRawRoot()).invoke(object17);
                    continue;
                }
                Object object20 = rule = ((IFn)const__16.getRawRoot()).invoke(seq_188482);
                rule = null;
                ((IFn)const__9.getRawRoot()).invoke(db2, prog, oprog, object20, apred, input2, sched_fn, src, ans, ins, top_bounds, nested_bounds);
                Object object21 = seq_188482;
                seq_188482 = null;
                i_18851 = 0L;
                count_18850 = 0L;
                chunk_18849 = null;
                seq_18848 = ((IFn)const__17.getRawRoot()).invoke(object21);
            }
        }
        return null;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9, Object object10, Object object11) {
        Object object12 = object;
        object = null;
        Object object13 = object2;
        object2 = null;
        Object object14 = object3;
        object3 = null;
        Object object15 = object4;
        object4 = null;
        Object object16 = object5;
        object5 = null;
        Object object17 = object6;
        object6 = null;
        Object object18 = object7;
        object7 = null;
        Object object19 = object8;
        object8 = null;
        Object object20 = object9;
        object9 = null;
        Object object21 = object10;
        object10 = null;
        Object object22 = object11;
        object11 = null;
        return datalog$eval_query.invokeStatic(object12, object13, object14, object15, object16, object17, object18, object19, object20, object21, object22);
    }
}


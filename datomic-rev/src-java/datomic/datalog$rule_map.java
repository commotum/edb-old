/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Indexed
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Indexed;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.datalog$rule_map$fn__18758;
import java.util.Arrays;

public final class datalog$rule_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*read-eval*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"group-by");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"ffirst");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"reduce-kv");
    public static final Var const__7 = RT.var((String)"datomic.datalog", (String)"add-rule");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"=");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"count");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__21 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"apply"), Symbol.intern(null, (String)"="), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"map"), ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"comp"), Symbol.intern(null, (String)"count"), Symbol.intern(null, (String)"first")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 29})), Symbol.intern(null, (String)"v")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 24}))))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 15}));
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__27 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object rules) {
        Object rules2;
        Object rules3;
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(rules);
        if (object2 != null && object2 != Boolean.FALSE) {
            ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3, (Object)Boolean.FALSE));
            Object object3 = rules;
            rules = null;
            object = ((IFn)new datalog$rule_map$fn__18758(object3)).invoke();
        } else {
            object = rules;
            rules = null;
        }
        Object object4 = rules3 = object;
        rules3 = null;
        Object object5 = rules2 = ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), object4);
        rules2 = null;
        Object rules4 = ((IFn)const__6.getRawRoot()).invoke(const__7.getRawRoot(), (Object)PersistentArrayMap.EMPTY, object5);
        Object seq_18760 = ((IFn)const__8.getRawRoot()).invoke(rules4);
        Object chunk_18761 = null;
        long count_18762 = 0L;
        long i_18763 = 0L;
        while (true) {
            Object v;
            Object temp__5457__auto__18772;
            if (i_18763 < count_18762) {
                Object v2;
                Object vec__18764 = ((Indexed)chunk_18761).nth(RT.uncheckedIntCast((long)i_18763));
                Object k = RT.nth((Object)vec__18764, (int)RT.uncheckedIntCast((long)0L), null);
                Object object6 = vec__18764;
                vec__18764 = null;
                Object object7 = v2 = RT.nth((Object)object6, (int)RT.uncheckedIntCast((long)1L), null);
                v2 = null;
                Object object8 = ((IFn)const__13.getRawRoot()).invoke(const__14.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(const__17.getRawRoot(), const__18.getRawRoot()), object7));
                if (object8 == null || object8 == Boolean.FALSE) {
                    Object object9 = k;
                    k = null;
                    throw (Throwable)((Object)new AssertionError(((IFn)const__19.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__19.getRawRoot()).invoke((Object)"Arity mismatch for predicate: ", object9), (Object)"\n", ((IFn)const__20.getRawRoot()).invoke(const__21))));
                }
                Object object10 = seq_18760;
                seq_18760 = null;
                Object object11 = chunk_18761;
                chunk_18761 = null;
                ++i_18763;
                chunk_18761 = object11;
                seq_18760 = object10;
                continue;
            }
            Object object12 = seq_18760;
            seq_18760 = null;
            Object object13 = temp__5457__auto__18772 = ((IFn)const__8.getRawRoot()).invoke(object12);
            if (object13 == null || object13 == Boolean.FALSE) break;
            Object object14 = temp__5457__auto__18772;
            temp__5457__auto__18772 = null;
            Object seq_187602 = object14;
            Object object15 = ((IFn)const__23.getRawRoot()).invoke(seq_187602);
            if (object15 != null && object15 != Boolean.FALSE) {
                Object c__5719__auto__18771 = ((IFn)const__24.getRawRoot()).invoke(seq_187602);
                Object object16 = seq_187602;
                seq_187602 = null;
                Object object17 = c__5719__auto__18771;
                Object object18 = c__5719__auto__18771;
                c__5719__auto__18771 = null;
                i_18763 = (int)0L;
                count_18762 = RT.count((Object)object18);
                chunk_18761 = object17;
                seq_18760 = ((IFn)const__25.getRawRoot()).invoke(object16);
                continue;
            }
            Object vec__18767 = ((IFn)const__18.getRawRoot()).invoke(seq_187602);
            Object k = RT.nth((Object)vec__18767, (int)RT.uncheckedIntCast((long)0L), null);
            Object object19 = vec__18767;
            vec__18767 = null;
            Object object20 = v = RT.nth((Object)object19, (int)RT.uncheckedIntCast((long)1L), null);
            v = null;
            Object object21 = ((IFn)const__13.getRawRoot()).invoke(const__14.getRawRoot(), ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke(const__17.getRawRoot(), const__18.getRawRoot()), object20));
            if (object21 == null || object21 == Boolean.FALSE) {
                Object object22 = k;
                k = null;
                throw (Throwable)((Object)new AssertionError(((IFn)const__19.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__19.getRawRoot()).invoke((Object)"Arity mismatch for predicate: ", object22), (Object)"\n", ((IFn)const__20.getRawRoot()).invoke(const__21))));
            }
            Object object23 = seq_187602;
            seq_187602 = null;
            i_18763 = 0L;
            count_18762 = 0L;
            chunk_18761 = null;
            seq_18760 = ((IFn)const__27.getRawRoot()).invoke(object23);
        }
        Object var3_3 = null;
        return rules4;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$rule_map.invokeStatic(object2);
    }
}


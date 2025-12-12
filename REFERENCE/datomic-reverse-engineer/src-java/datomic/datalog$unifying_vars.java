/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.PersistentList
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.PersistentList;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.datalog$unifying_vars$fn__18438;
import java.util.Arrays;

public final class datalog$unifying_vars
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__3 = RT.var((String)"datomic.datalog", (String)"source?");
    public static final AFn const__4 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"or-join"), Symbol.intern(null, (String)"not-join")});
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"flatten");
    public static final AFn const__6 = (AFn)PersistentHashSet.create((Object[])new Object[]{Symbol.intern(null, (String)"not"), Symbol.intern(null, (String)"and")});
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__8 = RT.var((String)"datomic.datalog", (String)"unifying-vars");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"=");
    public static final AFn const__10 = (AFn)Symbol.intern(null, (String)"or");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"mapv");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final Object const__15 = ((IObj)PersistentList.create(Arrays.asList(Symbol.intern(null, (String)"apply"), Symbol.intern(null, (String)"="), Symbol.intern(null, (String)"uvs")))).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"column"), 15}));
    public static final Keyword const__16 = RT.keyword(null, (String)"else");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__18 = RT.var((String)"datomic.datalog", (String)"variable?");

    public static Object invokeStatic(Object p__18431) {
        Object object;
        Object object2;
        Object object3 = p__18431;
        p__18431 = null;
        Object vec__18432 = object3;
        Object seq__18433 = ((IFn)const__0.getRawRoot()).invoke(vec__18432);
        Object first__18434 = ((IFn)const__1.getRawRoot()).invoke(seq__18433);
        Object object4 = seq__18433;
        seq__18433 = null;
        Object seq__184332 = ((IFn)const__2.getRawRoot()).invoke(object4);
        Object object5 = first__18434;
        first__18434 = null;
        Object p = object5;
        Object object6 = seq__184332;
        seq__184332 = null;
        Object cs = object6;
        Object object7 = vec__18432;
        vec__18432 = null;
        Object c = object7;
        Object object8 = p;
        p = null;
        Object object9 = ((IFn)const__3.getRawRoot()).invoke(object8);
        if (object9 != null && object9 != Boolean.FALSE) {
            object2 = cs;
            cs = null;
        } else {
            object2 = c;
            c = null;
        }
        Object vec__18435 = object2;
        Object seq__18436 = ((IFn)const__0.getRawRoot()).invoke(vec__18435);
        Object first__18437 = ((IFn)const__1.getRawRoot()).invoke(seq__18436);
        Object object10 = seq__18436;
        seq__18436 = null;
        Object seq__184362 = ((IFn)const__2.getRawRoot()).invoke(object10);
        Object object11 = first__18437;
        first__18437 = null;
        Object p2 = object11;
        Object object12 = seq__184362;
        seq__184362 = null;
        Object cs2 = object12;
        Object object13 = vec__18435;
        vec__18435 = null;
        Object c2 = object13;
        Object object14 = ((IFn)const__4).invoke(p2);
        if (object14 != null && object14 != Boolean.FALSE) {
            Object object15 = cs2;
            cs2 = null;
            object = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object15));
        } else {
            Object object16 = ((IFn)const__6).invoke(p2);
            if (object16 != null && object16 != Boolean.FALSE) {
                Object object17 = cs2;
                cs2 = null;
                object = ((IFn)const__7.getRawRoot()).invoke(const__8.getRawRoot(), object17);
            } else {
                Object object18 = p2;
                p2 = null;
                if (Util.equiv((Object)const__10, (Object)object18)) {
                    Object object19 = cs2;
                    cs2 = null;
                    Object uvs = ((IFn)const__11.getRawRoot()).invoke((Object)new datalog$unifying_vars$fn__18438(), object19);
                    Object object20 = ((IFn)const__12.getRawRoot()).invoke(const__9.getRawRoot(), uvs);
                    if (object20 == null || object20 == Boolean.FALSE) {
                        throw (Throwable)((Object)new AssertionError(((IFn)const__13.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__13.getRawRoot()).invoke((Object)"All clauses in 'or' must use same set of vars, had ", uvs), (Object)"\n", ((IFn)const__14.getRawRoot()).invoke(const__15))));
                    }
                    Object object21 = uvs;
                    uvs = null;
                    object = ((IFn)const__1.getRawRoot()).invoke(object21);
                } else {
                    Keyword keyword = const__16;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        Object object22 = c2;
                        c2 = null;
                        object = ((IFn)const__17.getRawRoot()).invoke(const__18.getRawRoot(), ((IFn)const__5.getRawRoot()).invoke(object22));
                    } else {
                        object = null;
                    }
                }
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$unifying_vars.invokeStatic(object2);
    }
}


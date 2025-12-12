/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class datalog$used_srcs
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"not-join-clause?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__5 = RT.var((String)"datomic.datalog", (String)"used-srcs");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"nnext");
    public static final AFn const__7 = (AFn)Symbol.intern(null, (String)"$");
    public static final Keyword const__8 = RT.keyword(null, (String)"else");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"flatten");

    public static Object invokeStatic(Object srcs, Object c) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(c);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = srcs;
            srcs = null;
            Object object4 = c;
            c = null;
            object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), object3), ((IFn)const__6.getRawRoot()).invoke(object4))), (Object)const__7);
        } else {
            Keyword keyword = const__8;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object5 = srcs;
                srcs = null;
                Object object6 = c;
                c = null;
                object = ((IFn)const__9.getRawRoot()).invoke(object5, ((IFn)const__10.getRawRoot()).invoke(object6));
            } else {
                object = null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datalog$used_srcs.invokeStatic(object3, object4);
    }
}


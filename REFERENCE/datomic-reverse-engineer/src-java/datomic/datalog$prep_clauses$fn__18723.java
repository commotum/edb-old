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
 *  clojure.lang.Util
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
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$prep_clauses$fn__18723
extends AFunction {
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"or-join");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__6 = RT.var((String)"datomic.datalog", (String)"or-join->rule-preds");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"ffirst");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__10 = RT.var((String)"datomic.datalog", (String)"add-rule");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"flatten");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"merge");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"meta");

    public Object invoke(Object p__18722, Object c) {
        IPersistentVector iPersistentVector;
        Object object = p__18722;
        p__18722 = null;
        Object vec__18724 = object;
        Object rm = RT.nth((Object)vec__18724, (int)RT.uncheckedIntCast((long)0L), null);
        Object object2 = vec__18724;
        vec__18724 = null;
        Object cs = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
        if (Util.equiv((Object)const__4, (Object)((IFn)const__5.getRawRoot()).invoke(c))) {
            Object rm2;
            Object preds = ((IFn)const__6.getRawRoot()).invoke(c);
            Object vec__18727 = ((IFn)const__7.getRawRoot()).invoke(preds);
            Object seq__18728 = ((IFn)const__8.getRawRoot()).invoke(vec__18727);
            Object first__18729 = ((IFn)const__5.getRawRoot()).invoke(seq__18728);
            Object object3 = seq__18728;
            seq__18728 = null;
            Object seq__187282 = ((IFn)const__9.getRawRoot()).invoke(object3);
            Object object4 = first__18729;
            first__18729 = null;
            Object rname = object4;
            seq__187282 = null;
            Object object5 = vec__18727;
            vec__18727 = null;
            Object head = object5;
            Object object6 = rm;
            rm = null;
            Object object7 = rname;
            rname = null;
            Object object8 = preds;
            preds = null;
            Object object9 = rm2 = ((IFn)const__10.getRawRoot()).invoke(object6, object7, object8);
            rm2 = null;
            Object object10 = cs;
            cs = null;
            Object object11 = ((IFn)const__13.getRawRoot()).invoke(head);
            Object object12 = c;
            c = null;
            Object object13 = head;
            head = null;
            iPersistentVector = Tuple.create((Object)object9, (Object)((IFn)const__11.getRawRoot()).invoke(object10, ((IFn)const__12.getRawRoot()).invoke(object11, ((IFn)const__14.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(object12), ((IFn)const__15.getRawRoot()).invoke(object13)))));
        } else {
            Object object14 = rm;
            rm = null;
            Object object15 = cs;
            cs = null;
            Object object16 = c;
            c = null;
            iPersistentVector = Tuple.create((Object)object14, (Object)((IFn)const__11.getRawRoot()).invoke(object15, object16));
        }
        return iPersistentVector;
    }
}


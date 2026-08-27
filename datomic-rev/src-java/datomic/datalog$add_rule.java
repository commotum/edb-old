/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.datalog$add_rule$fn__18746;
import datomic.datalog$add_rule$rcnt__18738;

public final class datalog$add_rule
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"max");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"vary-meta");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__7 = RT.keyword(null, (String)"reqcnt");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"vec");

    public static Object invokeStatic(Object rm, Object rname, Object preds) {
        Object object;
        datalog$add_rule$rcnt__18738 rcnt;
        Object heads = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), preds);
        datalog$add_rule$rcnt__18738 datalog$add_rule$rcnt__18738 = rcnt = new datalog$add_rule$rcnt__18738();
        rcnt = null;
        Object object2 = heads;
        heads = null;
        Object rmax = ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke((Object)datalog$add_rule$rcnt__18738, object2));
        Object object3 = rname;
        rname = null;
        Object G__18743 = object3;
        if (Numbers.isPos((Object)rmax)) {
            Object object4 = G__18743;
            G__18743 = null;
            Object object5 = rmax;
            rmax = null;
            object = ((IFn)const__5.getRawRoot()).invoke(object4, const__6.getRawRoot(), (Object)const__7, object5);
        } else {
            object = G__18743;
            G__18743 = null;
        }
        Object rname2 = object;
        Object object6 = rm;
        rm = null;
        Object object7 = preds;
        preds = null;
        Object vec__18734 = ((IFn)const__8.getRawRoot()).invoke((Object)new datalog$add_rule$fn__18746(), (Object)Tuple.create((Object)object6, (Object)PersistentVector.EMPTY), object7);
        Object rm2 = RT.nth((Object)vec__18734, (int)RT.uncheckedIntCast((long)0L), null);
        Object object8 = vec__18734;
        vec__18734 = null;
        Object preds2 = RT.nth((Object)object8, (int)RT.uncheckedIntCast((long)1L), null);
        Object object9 = rm2;
        rm2 = null;
        Object object10 = rname2;
        rname2 = null;
        Object object11 = preds2;
        preds2 = null;
        return ((IFn)const__6.getRawRoot()).invoke(object9, object10, ((IFn)const__12.getRawRoot()).invoke(object11));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datalog$add_rule.invokeStatic(object4, object5, object6);
    }
}


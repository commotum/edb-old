/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db.Attribute;
import datomic.iter.Iter;
import datomic.pull$ra__GT_e$fn__18913;
import datomic.pull$ra__GT_e$mk_iter__18906;

public final class pull$ra__GT_e
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__1 = RT.var((String)"datomic.pull", (String)"nilify-empty");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__5 = RT.var((String)"datomic.pull", (String)"limit-iterable");
    public static final Var const__6 = RT.var((String)"datomic.iter", (String)"iterable");

    public static Object invokeStatic(Object db2, Object r, Object attr, Object xf, Object limit2, Object valfn) {
        Object object;
        Object object2 = valfn;
        valfn = null;
        IFn iFn = (IFn)object2;
        Object object3 = attr;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object temp__5457__auto__18917;
            Object object4 = r;
            r = null;
            Object rid = ((IFn)const__0.getRawRoot()).invoke(db2, object4);
            Object attrid = ((Attribute)attr).id();
            Object object5 = db2;
            db2 = null;
            Object object6 = rid;
            rid = null;
            Object object7 = attrid;
            attrid = null;
            pull$ra__GT_e$mk_iter__18906 mk_iter = new pull$ra__GT_e$mk_iter__18906(object5, object6, object7);
            Object object8 = temp__5457__auto__18917 = ((IFn)mk_iter).invoke();
            if (object8 != null && object8 != Boolean.FALSE) {
                Object object9 = temp__5457__auto__18917;
                temp__5457__auto__18917 = null;
                Object iter2 = object9;
                Object object10 = attr;
                attr = null;
                Object object11 = ((Attribute)object10).isComponent;
                if (object11 != null && object11 != Boolean.FALSE) {
                    Object object12 = xf;
                    xf = null;
                    Object object13 = iter2;
                    iter2 = null;
                    object = ((IFn)object12).invoke(((Iter)object13).get());
                } else {
                    Object object14 = xf;
                    xf = null;
                    Object object15 = limit2;
                    limit2 = null;
                    pull$ra__GT_e$mk_iter__18906 pull$ra__GT_e$mk_iter__18906 = mk_iter;
                    mk_iter = null;
                    object = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)new pull$ra__GT_e$fn__18913(object14), ((IFn)const__4.getRawRoot()).invoke((Object)PersistentVector.EMPTY), ((IFn)const__5.getRawRoot()).invoke(object15, ((IFn)const__6.getRawRoot()).invoke((Object)pull$ra__GT_e$mk_iter__18906)))));
                }
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return iFn.invoke(object);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        Object object7 = object;
        object = null;
        Object object8 = object2;
        object2 = null;
        Object object9 = object3;
        object3 = null;
        Object object10 = object4;
        object4 = null;
        Object object11 = object5;
        object5 = null;
        Object object12 = object6;
        object6 = null;
        return pull$ra__GT_e.invokeStatic(object7, object8, object9, object10, object11, object12);
    }
}


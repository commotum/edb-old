/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class common$result_xform
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"comp");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"drop");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"take");

    public static Object invokeStatic(Object offset, Object limit2, Object f) {
        Object object;
        Object object2;
        Object and__5236__auto__9268;
        Object object3;
        Object object4;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object5 = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        Object object6 = const__3.getRawRoot();
        Object object7 = offset;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = offset;
            offset = null;
            object4 = ((IFn)const__4.getRawRoot()).invoke(object8);
        } else {
            object4 = null;
        }
        Object object9 = f;
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = f;
            f = null;
            object3 = ((IFn)const__5.getRawRoot()).invoke(object10);
        } else {
            object3 = null;
        }
        Object object11 = and__5236__auto__9268 = limit2;
        if (object11 != null && object11 != Boolean.FALSE) {
            object2 = ((IFn)const__6.getRawRoot()).invoke((Object)(Numbers.isNeg((Object)limit2) ? Boolean.TRUE : Boolean.FALSE));
        } else {
            object2 = and__5236__auto__9268;
            Object var3_3 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object12 = limit2;
            limit2 = null;
            object = ((IFn)const__8.getRawRoot()).invoke(object12);
        } else {
            object = null;
        }
        return iFn.invoke(object5, iFn2.invoke(object6, (Object)Tuple.create((Object)object4, (Object)object3, object)));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return common$result_xform.invokeStatic(object4, object5, object6);
    }
}


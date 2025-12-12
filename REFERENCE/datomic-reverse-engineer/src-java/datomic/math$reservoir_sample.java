/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class math$reservoir_sample
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"drop");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"datomic.math", (String)"uniform");
    public static final Object const__6 = 0L;
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"assoc!");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"rest");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"persistent!");

    public static Object invokeStatic(Object ct, Object coll) {
        Object result2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(ct, coll)));
        Object n = ct;
        Object object = coll;
        coll = null;
        Object coll2 = ((IFn)const__3.getRawRoot()).invoke(ct, object);
        while (true) {
            Object object2;
            Object object3 = ((IFn)const__4.getRawRoot()).invoke(coll2);
            if (object3 == null || object3 == Boolean.FALSE) break;
            long pos = ((IFn.OOL)const__5.getRawRoot()).invokePrim(const__6, (Object)Numbers.inc((Object)n));
            if (Numbers.lt((long)pos, (Object)ct)) {
                Object object4 = result2;
                result2 = null;
                object2 = ((IFn)const__9.getRawRoot()).invoke(object4, (Object)Numbers.num((long)pos), ((IFn)const__10.getRawRoot()).invoke(coll2));
            } else {
                object2 = result2;
                result2 = null;
            }
            Object object5 = n;
            n = null;
            Object object6 = coll2;
            coll2 = null;
            coll2 = ((IFn)const__11.getRawRoot()).invoke(object6);
            n = Numbers.inc((Object)object5);
            result2 = object2;
        }
        Object object7 = result2;
        result2 = null;
        return ((IFn)const__12.getRawRoot()).invoke(object7);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return math$reservoir_sample.invokeStatic(object3, object4);
    }
}


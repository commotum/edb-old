/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class pull$denormalize_kw
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"=");
    public static final Object const__1 = RT.classForName((String)"clojure.lang.Keyword");
    public static final Object const__2 = RT.classForName((String)"clojure.lang.Symbol");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Object const__4 = RT.classForName((String)"java.lang.String");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object kw, Object type) {
        Object object;
        Object pred__19000 = const__0.getRawRoot();
        Object object2 = type;
        type = null;
        Object expr__19001 = object2;
        Object object3 = ((IFn)pred__19000).invoke(const__1, expr__19001);
        if (object3 != null && object3 != Boolean.FALSE) {
            object = kw;
            kw = null;
        } else {
            Object object4 = ((IFn)pred__19000).invoke(const__2, expr__19001);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = kw;
                kw = null;
                object = ((IFn)const__3.getRawRoot()).invoke(object5);
            } else {
                Object object6 = pred__19000;
                pred__19000 = null;
                Object object7 = ((IFn)object6).invoke(const__4, expr__19001);
                if (object7 != null && object7 != Boolean.FALSE) {
                    Object object8 = kw;
                    kw = null;
                    object = ((IFn)const__5.getRawRoot()).invoke(object8);
                } else {
                    Object object9 = expr__19001;
                    expr__19001 = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__5.getRawRoot()).invoke((Object)"No matching clause: ", object9));
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return pull$denormalize_kw.invokeStatic(object3, object4);
    }
}


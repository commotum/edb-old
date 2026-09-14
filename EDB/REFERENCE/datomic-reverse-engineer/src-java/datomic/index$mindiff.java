/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$mindiff
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"strdiff");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"vector?");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"vecdiff");
    public static final Keyword const__4 = RT.keyword(null, (String)"default");

    public static Object invokeStatic(Object minv, Object maxv) {
        Object object;
        Object object2;
        Object and__5236__auto__15393;
        Object object3 = and__5236__auto__15393 = ((IFn)const__0.getRawRoot()).invoke(minv);
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = ((IFn)const__0.getRawRoot()).invoke(maxv);
        } else {
            object2 = and__5236__auto__15393;
            and__5236__auto__15393 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object4 = minv;
            minv = null;
            Object object5 = maxv;
            maxv = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object4, object5);
        } else {
            Object object6;
            Object and__5236__auto__15394;
            Object object7 = and__5236__auto__15394 = ((IFn)const__2.getRawRoot()).invoke(minv);
            if (object7 != null && object7 != Boolean.FALSE) {
                object6 = ((IFn)const__2.getRawRoot()).invoke(maxv);
            } else {
                object6 = and__5236__auto__15394;
                Object var2_2 = null;
            }
            if (object6 != null && object6 != Boolean.FALSE) {
                Object object8 = minv;
                minv = null;
                Object object9 = maxv;
                maxv = null;
                object = ((IFn)const__3.getRawRoot()).invoke(object8, object9);
            } else {
                Keyword keyword = const__4;
                if (keyword != null && keyword != Boolean.FALSE) {
                    object = maxv;
                    maxv = null;
                } else {
                    object = null;
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
        return index$mindiff.invokeStatic(object3, object4);
    }
}


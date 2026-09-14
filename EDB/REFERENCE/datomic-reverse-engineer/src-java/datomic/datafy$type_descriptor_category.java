/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class datafy$type_descriptor_category
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"ancestors");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Object const__3 = RT.classForName((String)"java.util.Map");
    public static final Keyword const__4 = RT.keyword(null, (String)"map");
    public static final Object const__5 = RT.classForName((String)"java.util.Collection");
    public static final Keyword const__6 = RT.keyword(null, (String)"list");
    public static final Keyword const__7 = RT.keyword(null, (String)"enum");
    public static final Keyword const__8 = RT.keyword(null, (String)"self");
    public static final Keyword const__9 = RT.keyword(null, (String)"default");
    public static final Keyword const__10 = RT.keyword(null, (String)"bean");

    public static Object invokeStatic(Object c) {
        Object object;
        Object object2;
        Object or__5238__auto__17181;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object3 = or__5238__auto__17181 = ((IFn)const__1.getRawRoot()).invoke(c);
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = or__5238__auto__17181;
            or__5238__auto__17181 = null;
        } else {
            object2 = PersistentHashSet.EMPTY;
        }
        Object anc = iFn.invoke(object2, c);
        Object object4 = ((IFn)const__2.getRawRoot()).invoke(anc, const__3);
        if (object4 != null && object4 != Boolean.FALSE) {
            object = const__4;
        } else {
            Object object5 = anc;
            anc = null;
            Object object6 = ((IFn)const__2.getRawRoot()).invoke(object5, const__5);
            if (object6 != null && object6 != Boolean.FALSE) {
                object = const__6;
            } else if (((Class)c).isEnum()) {
                object = const__7;
            } else if (((Class)c).isPrimitive()) {
                object = const__8;
            } else {
                Object object7 = c;
                c = null;
                if (((Class)object7).getPackage().getName().startsWith("java")) {
                    object = const__8;
                } else {
                    Keyword keyword = const__9;
                    object = keyword != null && keyword != Boolean.FALSE ? const__10 : null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$type_descriptor_category.invokeStatic(object2);
    }
}


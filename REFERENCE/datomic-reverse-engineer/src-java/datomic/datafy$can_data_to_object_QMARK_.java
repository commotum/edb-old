/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class datafy$can_data_to_object_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"get-method");
    public static final Var const__1 = RT.var((String)"datomic.datafy", (String)"data-to-object");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not=");
    public static final Keyword const__3 = RT.keyword(null, (String)"atom");
    public static final Keyword const__4 = RT.keyword(null, (String)"map");
    public static final Var const__5 = RT.var((String)"clojure.string", (String)"starts-with?");

    public static Object invokeStatic(Object cls) {
        Object object;
        Object or__5238__auto__17289;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), null);
        Object object3 = or__5238__auto__17289 = ((IFn)const__2.getRawRoot()).invoke(object2, ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), (Object)Tuple.create((Object)const__3, (Object)cls)));
        if (object3 != null && object3 != Boolean.FALSE) {
            object = or__5238__auto__17289;
            or__5238__auto__17289 = null;
        } else {
            Object or__5238__auto__17288;
            Object object4 = object2;
            object2 = null;
            Object object5 = or__5238__auto__17288 = ((IFn)const__2.getRawRoot()).invoke(object4, ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), (Object)Tuple.create((Object)const__4, (Object)cls)));
            if (object5 != null && object5 != Boolean.FALSE) {
                object = or__5238__auto__17288;
                or__5238__auto__17288 = null;
            } else {
                Object object6 = cls;
                cls = null;
                object = ((IFn)const__5.getRawRoot()).invoke((Object)((Class)object6).getName(), (Object)"java.");
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datafy$can_data_to_object_QMARK_.invokeStatic(object2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class datafy$define_list_valued_property_to_object
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"do");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"clojure.core", (String)"alter-var-root");
    public static final AFn const__5 = (AFn)Symbol.intern(null, (String)"var");
    public static final AFn const__6 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"list-property-types");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core", (String)"assoc");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__10 = (AFn)Symbol.intern((String)"clojure.core", (String)"defmethod");
    public static final AFn const__11 = (AFn)Symbol.intern((String)"d", (String)"property-to-object");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"_");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"_");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"val");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"_");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"clojure.core", (String)"mapv");
    public static final AFn const__17 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)"item");
    public static final AFn const__19 = (AFn)Symbol.intern((String)"d", (String)"data-to-object");
    public static final AFn const__20 = (AFn)Symbol.intern(null, (String)"item");
    public static final AFn const__21 = (AFn)Symbol.intern(null, (String)"val");

    public static Object invokeStatic(Object container, Object property2, Object list_value_type) {
        Object object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__4), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__5), ((IFn)const__2.getRawRoot()).invoke((Object)const__6)))), ((IFn)const__2.getRawRoot()).invoke((Object)const__7), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(container), ((IFn)const__2.getRawRoot()).invoke(property2))))), ((IFn)const__2.getRawRoot()).invoke(list_value_type))));
        Object object2 = container;
        container = null;
        Object object3 = property2;
        property2 = null;
        Object object4 = list_value_type;
        list_value_type = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), object, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__10), ((IFn)const__2.getRawRoot()).invoke((Object)const__11), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object2), ((IFn)const__2.getRawRoot()).invoke(object3))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__12), ((IFn)const__2.getRawRoot()).invoke((Object)const__13), ((IFn)const__2.getRawRoot()).invoke((Object)const__14), ((IFn)const__2.getRawRoot()).invoke((Object)const__15))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__16), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__17), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__18))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__19), ((IFn)const__2.getRawRoot()).invoke((Object)const__20), ((IFn)const__2.getRawRoot()).invoke(object4))))))), ((IFn)const__2.getRawRoot()).invoke((Object)const__21)))))))));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return datafy$define_list_valued_property_to_object.invokeStatic(object4, object5, object6);
    }
}


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

public final class datafy$define_map_valued_property_to_object
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"list");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"do");
    public static final AFn const__4 = (AFn)Symbol.intern((String)"clojure.core", (String)"alter-var-root");
    public static final AFn const__5 = (AFn)Symbol.intern(null, (String)"var");
    public static final AFn const__6 = (AFn)Symbol.intern((String)"datomic.datafy", (String)"map-property-types");
    public static final AFn const__7 = (AFn)Symbol.intern((String)"clojure.core", (String)"assoc");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"vector");
    public static final AFn const__10 = (AFn)Symbol.intern((String)"clojure.core", (String)"defmethod");
    public static final AFn const__11 = (AFn)Symbol.intern((String)"d", (String)"property-to-object");
    public static final AFn const__12 = (AFn)Symbol.intern(null, (String)"_");
    public static final AFn const__13 = (AFn)Symbol.intern(null, (String)"_");
    public static final AFn const__14 = (AFn)Symbol.intern(null, (String)"val");
    public static final AFn const__15 = (AFn)Symbol.intern(null, (String)"_");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"clojure.core", (String)"reduce");
    public static final AFn const__17 = (AFn)Symbol.intern((String)"clojure.core", (String)"fn");
    public static final AFn const__18 = (AFn)Symbol.intern(null, (String)"m");
    public static final AFn const__19 = (AFn)Symbol.intern(null, (String)"k");
    public static final AFn const__20 = (AFn)Symbol.intern(null, (String)"v");
    public static final AFn const__21 = (AFn)Symbol.intern((String)"clojure.core", (String)"assoc");
    public static final AFn const__22 = (AFn)Symbol.intern(null, (String)"m");
    public static final AFn const__23 = (AFn)Symbol.intern(null, (String)"k");
    public static final AFn const__24 = (AFn)Symbol.intern((String)"d", (String)"data-to-object");
    public static final AFn const__25 = (AFn)Symbol.intern(null, (String)"v");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final AFn const__27 = (AFn)Symbol.intern(null, (String)"val");

    public static Object invokeStatic(Object container, Object property2, Object map_key_type, Object map_value_type) {
        Object object = map_key_type;
        map_key_type = null;
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__4), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__5), ((IFn)const__2.getRawRoot()).invoke((Object)const__6)))), ((IFn)const__2.getRawRoot()).invoke((Object)const__7), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(container), ((IFn)const__2.getRawRoot()).invoke(property2))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object), ((IFn)const__2.getRawRoot()).invoke(map_value_type))))))));
        Object object3 = container;
        container = null;
        Object object4 = property2;
        property2 = null;
        Object object5 = map_value_type;
        map_value_type = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3), object2, ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__10), ((IFn)const__2.getRawRoot()).invoke((Object)const__11), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object3), ((IFn)const__2.getRawRoot()).invoke(object4))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__12), ((IFn)const__2.getRawRoot()).invoke((Object)const__13), ((IFn)const__2.getRawRoot()).invoke((Object)const__14), ((IFn)const__2.getRawRoot()).invoke((Object)const__15))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__16), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__17), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__18), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__9.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__19), ((IFn)const__2.getRawRoot()).invoke((Object)const__20))))))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__21), ((IFn)const__2.getRawRoot()).invoke((Object)const__22), ((IFn)const__2.getRawRoot()).invoke((Object)const__23), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__24), ((IFn)const__2.getRawRoot()).invoke((Object)const__25), ((IFn)const__2.getRawRoot()).invoke(object5)))))))))), ((IFn)const__2.getRawRoot()).invoke(((IFn)const__8.getRawRoot()).invoke(const__26.getRawRoot(), ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke()))), ((IFn)const__2.getRawRoot()).invoke((Object)const__27)))))))));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return datafy$define_map_valued_property_to_object.invokeStatic(object5, object6, object7, object8);
    }
}


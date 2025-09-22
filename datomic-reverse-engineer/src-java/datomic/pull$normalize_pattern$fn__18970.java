/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.pull$normalize_pattern$fn__18970$fn__18971;
import java.util.Map;

public final class pull$normalize_pattern$fn__18970
extends AFunction {
    Object direction;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"reduce-kv");
    public static final Var const__3 = RT.var((String)"datomic.pull", (String)"normalize-attr");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"keyword");
    public static final Keyword const__11 = RT.keyword(null, (String)"*");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__13 = RT.keyword(null, (String)"wildcard");
    public static final Keyword const__14 = RT.keyword(null, (String)"dbid");
    public static final Keyword const__15 = RT.keyword((String)"db", (String)"id");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"get-in");
    public static final Var const__17 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__18 = RT.keyword((String)"pull", (String)"duplicate-attribute");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"assoc-in");
    public static final Keyword const__21 = RT.keyword(null, (String)"limit");
    public static final Keyword const__22 = RT.keyword(null, (String)"keyfn");
    public static final Keyword const__23 = RT.keyword(null, (String)"valfn");

    public pull$normalize_pattern$fn__18970(Object object) {
        this.direction = object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object m, Object i) {
        Object object;
        pull$normalize_pattern$fn__18970 this_;
        if (i instanceof Map) {
            Object object2 = m;
            m = null;
            Object object3 = i;
            i = null;
            this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)new pull$normalize_pattern$fn__18970$fn__18971(this_.direction), object2, object3);
            return object;
        }
        Object object4 = i;
        i = null;
        Object vec__18976 = ((IFn)const__3.getRawRoot()).invoke(object4);
        Object attr_name = RT.nth((Object)vec__18976, (int)RT.intCast((long)0L), null);
        Object limit2 = RT.nth((Object)vec__18976, (int)RT.intCast((long)1L), null);
        Object valfn = RT.nth((Object)vec__18976, (int)RT.intCast((long)2L), null);
        Object object5 = vec__18976;
        vec__18976 = null;
        Object keyfn = RT.nth((Object)object5, (int)RT.intCast((long)3L), null);
        Object attr_name_type = ((IFn)const__9.getRawRoot()).invoke(attr_name);
        Object G__18979 = ((IFn)const__10.getRawRoot()).invoke(attr_name);
        switch (Util.hash((Object)G__18979) >> 0 & 1) {
            case 0: {
                if (G__18979 != const__11) break;
                Object object6 = m;
                m = null;
                Object object7 = attr_name_type;
                Object object8 = attr_name_type;
                attr_name_type = null;
                this_ = null;
                object = ((IFn)const__12.getRawRoot()).invoke(object6, (Object)const__13, object7, (Object)const__14, object8);
                return object;
            }
            case 1: {
                if (G__18979 != const__15) break;
                Object object9 = m;
                m = null;
                Object object10 = attr_name_type;
                attr_name_type = null;
                this_ = null;
                object = ((IFn)const__12.getRawRoot()).invoke(object9, (Object)const__14, object10);
                return object;
            }
        }
        Object object11 = ((IFn)const__16.getRawRoot()).invoke(m, (Object)Tuple.create((Object)((IFn)this_.direction).invoke(attr_name), (Object)attr_name));
        if (object11 != null && object11 != Boolean.FALSE) {
            Object object12 = attr_name;
            attr_name = null;
            this_ = null;
            object = ((IFn)const__17.getRawRoot()).invoke((Object)const__18, ((IFn)const__19.getRawRoot()).invoke((Object)"Multiple specifications for ", object12));
            return object;
        }
        Object object13 = m;
        m = null;
        Object object14 = ((IFn)this_.direction).invoke(attr_name);
        Object object15 = attr_name;
        attr_name = null;
        Object[] objectArray = new Object[6];
        objectArray[0] = const__21;
        Object object16 = limit2;
        limit2 = null;
        objectArray[1] = object16;
        objectArray[2] = const__22;
        Object object17 = keyfn;
        keyfn = null;
        objectArray[3] = object17;
        objectArray[4] = const__23;
        Object object18 = valfn;
        valfn = null;
        objectArray[5] = object18;
        this_ = null;
        object = ((IFn)const__20.getRawRoot()).invoke(object13, (Object)Tuple.create((Object)object14, (Object)object15), (Object)RT.mapUniqueKeys((Object[])objectArray));
        return object;
    }
}


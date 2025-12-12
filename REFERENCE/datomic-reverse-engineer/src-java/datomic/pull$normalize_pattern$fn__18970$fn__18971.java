/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class pull$normalize_pattern$fn__18970$fn__18971
extends AFunction {
    Object direction;
    public static final Var const__0 = RT.var((String)"datomic.pull", (String)"normalize-attr");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"get-in");
    public static final Var const__7 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__8 = RT.keyword((String)"pull", (String)"duplicate-attribute");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"assoc-in");
    public static final Keyword const__11 = RT.keyword(null, (String)"limit");
    public static final Keyword const__12 = RT.keyword(null, (String)"valfn");
    public static final Keyword const__13 = RT.keyword(null, (String)"keyfn");
    public static final Keyword const__14 = RT.keyword(null, (String)"subspec");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"sequential?");
    public static final Var const__16 = RT.var((String)"datomic.pull", (String)"normalize-pattern");
    public static final Var const__17 = RT.var((String)"datomic.pull", (String)"normalize-recur-limit");

    public pull$normalize_pattern$fn__18970$fn__18971(Object object) {
        this.direction = object;
    }

    public Object invoke(Object m, Object k, Object subspec) {
        Object object;
        pull$normalize_pattern$fn__18970$fn__18971 this_;
        Object object2 = k;
        k = null;
        Object vec__18972 = ((IFn)const__0.getRawRoot()).invoke(object2);
        Object attr_name = RT.nth((Object)vec__18972, (int)RT.intCast((long)0L), null);
        Object limit2 = RT.nth((Object)vec__18972, (int)RT.intCast((long)1L), null);
        Object valfn = RT.nth((Object)vec__18972, (int)RT.intCast((long)2L), null);
        Object object3 = vec__18972;
        vec__18972 = null;
        Object keyfn = RT.nth((Object)object3, (int)RT.intCast((long)3L), null);
        Object object4 = ((IFn)const__6.getRawRoot()).invoke(m, (Object)Tuple.create((Object)((IFn)this_.direction).invoke(attr_name), (Object)attr_name));
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = attr_name;
            attr_name = null;
            this_ = null;
            object = ((IFn)const__7.getRawRoot()).invoke((Object)const__8, ((IFn)const__9.getRawRoot()).invoke((Object)"Multiple specifications for ", object5));
        } else {
            Object object6;
            IFn iFn = (IFn)const__10.getRawRoot();
            Object object7 = m;
            m = null;
            Object object8 = ((IFn)this_.direction).invoke(attr_name);
            Object object9 = attr_name;
            attr_name = null;
            IPersistentVector iPersistentVector = Tuple.create((Object)object8, (Object)object9);
            Object[] objectArray = new Object[8];
            objectArray[0] = const__11;
            Object object10 = limit2;
            limit2 = null;
            objectArray[1] = object10;
            objectArray[2] = const__12;
            Object object11 = valfn;
            valfn = null;
            objectArray[3] = object11;
            objectArray[4] = const__13;
            Object object12 = keyfn;
            keyfn = null;
            objectArray[5] = object12;
            objectArray[6] = const__14;
            Object object13 = ((IFn)const__15.getRawRoot()).invoke(subspec);
            if (object13 != null && object13 != Boolean.FALSE) {
                Object object14 = subspec;
                subspec = null;
                object6 = ((IFn)const__16.getRawRoot()).invoke(object14);
            } else {
                Object object15 = subspec;
                subspec = null;
                object6 = ((IFn)const__17.getRawRoot()).invoke(object15);
            }
            objectArray[7] = object6;
            this_ = null;
            object = iFn.invoke(object7, (Object)iPersistentVector, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return object;
    }
}


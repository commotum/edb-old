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
import java.util.List;

public final class db$force_map_keywords$fn__13681
extends AFunction {
    Object db;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"integer?");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Keyword const__7 = RT.keyword(null, (String)"else");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"to-kw");
    public static final Var const__10 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__11 = RT.keyword((String)"db.error", (String)"map-key-collision");
    public static final Keyword const__12 = RT.keyword(null, (String)"input");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"assoc");

    public db$force_map_keywords$fn__13681(Object object) {
        this.db = object;
    }

    public Object invoke(Object m, Object p__13680) {
        Object object;
        Object object2 = p__13680;
        p__13680 = null;
        Object vec__13682 = object2;
        Object k = RT.nth((Object)vec__13682, (int)RT.uncheckedIntCast((long)0L), null);
        Object object3 = vec__13682;
        vec__13682 = null;
        Object v = RT.nth((Object)object3, (int)RT.uncheckedIntCast((long)1L), null);
        Object object4 = ((IFn)const__3.getRawRoot()).invoke(k);
        if (object4 != null && object4 != Boolean.FALSE) {
            object = k;
            k = null;
        } else if (k instanceof List) {
            Object object5 = k;
            k = null;
            object = ((IFn)const__6.getRawRoot()).invoke(this_.db, object5);
        } else {
            Keyword keyword = const__7;
            if (keyword != null && keyword != Boolean.FALSE) {
                Object object6 = k;
                k = null;
                object = ((IFn)const__8.getRawRoot()).invoke(object6);
            } else {
                object = null;
            }
        }
        Object kw = object;
        Object object7 = RT.get((Object)m, (Object)kw);
        if (object7 != null && object7 != Boolean.FALSE) {
            ((IFn)const__10.getRawRoot()).invoke((Object)const__11, (Object)"Key collision", (Object)RT.mapUniqueKeys((Object[])new Object[]{const__12, m}));
        }
        Object object8 = m;
        m = null;
        Object object9 = kw;
        kw = null;
        Object object10 = v;
        v = null;
        db$force_map_keywords$fn__13681 this_ = null;
        return ((IFn)const__13.getRawRoot()).invoke(object8, object9, object10);
    }
}


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
import datomic.db$windowed$fn__12800;
import datomic.db.IDb;

public final class db$windowed
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"filter-retractions");
    public static final Var const__2 = RT.var((String)"datomic.iter", (String)"take-while");
    public static final Var const__3 = RT.var((String)"datomic.iter", (String)"filter");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__6 = RT.keyword(null, (String)"filt");

    public static Object invokeStatic(Object db2, Object whilep, Object iter2) {
        Object iter3;
        Object object;
        Object object2;
        Object object3;
        Object or__5238__auto__12806;
        Object object4;
        Object object5 = ((IDb)db2).getRaw();
        IFn iFn = (IFn)(object5 != null && object5 != Boolean.FALSE ? const__0.getRawRoot() : const__1.getRawRoot());
        Object asof = ((IDb)db2).getAsOfT();
        Object since2 = ((IDb)db2).getSinceT();
        Object object6 = whilep;
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = whilep;
            whilep = null;
            Object object8 = iter2;
            iter2 = null;
            object4 = ((IFn)const__2.getRawRoot()).invoke(object7, object8);
        } else {
            object4 = iter2;
            iter2 = null;
        }
        Object iter4 = object4;
        Object object9 = or__5238__auto__12806 = asof;
        if (object9 != null && object9 != Boolean.FALSE) {
            object3 = or__5238__auto__12806;
            or__5238__auto__12806 = null;
        } else {
            object3 = since2;
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object10 = since2;
            since2 = null;
            Object object11 = asof;
            asof = null;
            Object object12 = iter4;
            iter4 = null;
            object2 = ((IFn)const__3.getRawRoot()).invoke((Object)new db$windowed$fn__12800(object10, object11), object12);
        } else {
            object2 = iter4;
            iter4 = null;
        }
        Object iter5 = object2;
        Object object13 = ((IDb)db2).getFilter();
        if (object13 != null && object13 != Boolean.FALSE) {
            Object object14 = ((IDb)db2).getFilter();
            Object object15 = db2;
            db2 = null;
            Object object16 = iter5;
            iter5 = null;
            object = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object14, ((IFn)const__5.getRawRoot()).invoke(object15, (Object)const__6, null)), object16);
        } else {
            object = iter5;
            iter5 = null;
        }
        Object object17 = iter3 = object;
        iter3 = null;
        return iFn.invoke(object17);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$windowed.invokeStatic(object4, object5, object6);
    }
}


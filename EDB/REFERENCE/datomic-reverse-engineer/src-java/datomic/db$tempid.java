/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLL
 *  clojure.lang.IFn$LO
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$tempid
extends AFunction
implements IFn.LO {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"make-tempid");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"next-id");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"tempid");
    public static final Var const__4 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__5 = RT.keyword((String)"db.error", (String)"not-a-partition");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object db2, Object part2) {
        Object object;
        Object pid;
        Object object2 = db2;
        db2 = null;
        Object object3 = pid = ((IFn)const__2.getRawRoot()).invoke(object2, part2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = pid;
            pid = null;
            object = ((IFn.LO)const__3.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object4)));
        } else {
            Object object5 = part2;
            part2 = null;
            object = ((IFn)const__4.getRawRoot()).invoke((Object)const__5, ((IFn)const__6.getRawRoot()).invoke((Object)"Can't find partition with id: ", object5));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$tempid.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(long part2) {
        return Numbers.num((long)((IFn.LLL)const__0.getRawRoot()).invokePrim(part2, RT.uncheckedLongCast((Object)((Number)((IFn)const__1.getRawRoot()).invoke()))));
    }

    public Object invoke(Object object) {
        return db$tempid.invokeStatic(RT.uncheckedLongCast((Object)((Number)object)));
    }

    public final Object invokePrim(long l) {
        return db$tempid.invokeStatic(l);
    }
}


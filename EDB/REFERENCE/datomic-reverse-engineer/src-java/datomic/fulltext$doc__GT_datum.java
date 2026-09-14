/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.IFn$OLO
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class fulltext$doc__GT_datum
extends AFunction
implements IFn.OLO {
    public static final Var const__0 = RT.var((String)"datomic.lucene", (String)"long-value");
    public static final Var const__1 = RT.var((String)"datomic.lucene", (String)"get-field");
    public static final Var const__2 = RT.var((String)"datomic.lucene", (String)"string-value");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"asserting-datum");

    public static Object invokeStatic(Object doc, long a) {
        Object e = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(doc, (Object)"e"));
        Object t = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(doc, (Object)"t"));
        Object object = doc;
        doc = null;
        Object v = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(object, (Object)"v"));
        Object object2 = e;
        e = null;
        Object object3 = v;
        v = null;
        Object object4 = t;
        t = null;
        return ((IFn.LLOLO)const__3.getRawRoot()).invokePrim(RT.longCast((Object)((Number)object2)), a, object3, RT.longCast((Object)((Number)object4)));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        return fulltext$doc__GT_datum.invokeStatic(object3, RT.longCast((Object)((Number)object2)));
    }

    public final Object invokePrim(Object object, long l) {
        Object object2 = object;
        object = null;
        return fulltext$doc__GT_datum.invokeStatic(object2, l);
    }
}


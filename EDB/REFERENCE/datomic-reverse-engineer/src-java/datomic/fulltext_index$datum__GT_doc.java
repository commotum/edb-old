/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
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
import datomic.impl.db.IDatum;

public final class fulltext_index$datum__GT_doc
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.lucene", (String)"document");
    public static final Var const__1 = RT.var((String)"datomic.lucene", (String)"long-field");
    public static final Var const__2 = RT.var((String)"datomic.lucene", (String)"string-field");
    public static final Keyword const__3 = RT.keyword(null, (String)"store");
    public static final Keyword const__4 = RT.keyword(null, (String)"index");
    public static final Keyword const__5 = RT.keyword(null, (String)"analyze");

    public static Object invokeStatic(Object datum2) {
        Object object = ((IFn.OLO)const__1.getRawRoot()).invokePrim((Object)"e", ((IDatum)datum2).getE());
        Object object2 = ((IFn.OLO)const__1.getRawRoot()).invokePrim((Object)"t", ((IDatum)datum2).getT());
        Object object3 = datum2;
        datum2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, ((IFn)const__2.getRawRoot()).invoke((Object)"v", ((IDatum)object3).getV(), (Object)const__3, (Object)Boolean.TRUE, (Object)const__4, (Object)Boolean.TRUE, (Object)const__5, (Object)Boolean.TRUE));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return fulltext_index$datum__GT_doc.invokeStatic(object2);
    }
}


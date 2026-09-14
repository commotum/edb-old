/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Delay
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Delay;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$find_alter_fn$fn__13209$fn__13210;

public final class db$find_alter_fn$fn__13209
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"disabled");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"update-in");
    public static final Keyword const__3 = RT.keyword(null, (String)"elements");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__5 = RT.keyword(null, (String)"attrPred");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc-in");

    public Object invoke(Object db2, Object aid, Object _, Object preds) {
        IPersistentVector iPersistentVector;
        if (Util.equiv((Object)const__1, (Object)preds)) {
            Object object = db2;
            db2 = null;
            Object object2 = aid;
            aid = null;
            iPersistentVector = Tuple.create((Object)((IFn)const__2.getRawRoot()).invoke(object, (Object)Tuple.create((Object)const__3, (Object)object2), const__4.getRawRoot(), (Object)const__5));
        } else {
            Object object = db2;
            IPersistentVector iPersistentVector2 = Tuple.create((Object)const__3, (Object)aid, (Object)const__5);
            Object object3 = aid;
            aid = null;
            Object object4 = db2;
            db2 = null;
            Object object5 = preds;
            preds = null;
            iPersistentVector = Tuple.create((Object)((IFn)const__6.getRawRoot()).invoke(object, (Object)iPersistentVector2, (Object)new Delay((IFn)new db$find_alter_fn$fn__13209$fn__13210(object3, object4, object5))));
        }
        return iPersistentVector;
    }
}


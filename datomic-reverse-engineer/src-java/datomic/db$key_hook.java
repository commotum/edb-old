/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$key_hook$fn__13040;
import datomic.db.IDbImpl;
import datomic.impl.db.IDatum;

public final class db$key_hook
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"update-in");
    public static final Keyword const__3 = RT.keyword(null, (String)"elements");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"prevent-ident-retarget!");

    public static Object invokeStatic(Object _, Object db2, Object d, Object _2) {
        Object object;
        if ((long)((IDatum)d).getP() == 0L) {
            Object object2 = db2;
            db2 = null;
            object = ((IFn)const__2.getRawRoot()).invoke(((IDbImpl)object2).growElements(Numbers.num((long)((IDatum)d).getE())), (Object)Tuple.create((Object)const__3, (Object)Numbers.num((long)((IDatum)d).getE())), (Object)new db$key_hook$fn__13040(d));
        } else {
            object = db2;
            db2 = null;
        }
        Object db3 = object;
        ((IFn)const__4.getRawRoot()).invoke(db3, (Object)Numbers.num((long)((IDatum)d).getE()), ((IDatum)d).getV());
        Object object3 = db3;
        db3 = null;
        Object object4 = ((IDatum)d).getV();
        Object object5 = d;
        d = null;
        return ((IDbImpl)object3).addKeyword(object4, Numbers.num((long)((IDatum)object5).getE()));
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
        return db$key_hook.invokeStatic(object5, object6, object7, object8);
    }
}


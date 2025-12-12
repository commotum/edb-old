/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db$set_element_fields$fn__13148;

public final class db$set_element_fields
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"update-in");
    public static final Keyword const__1 = RT.keyword(null, (String)"elements");

    public static Object invokeStatic(Object db2, Object aid, ISeq kvs) {
        Object object = db2;
        db2 = null;
        Object object2 = aid;
        aid = null;
        ISeq iSeq = kvs;
        kvs = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)Tuple.create((Object)const__1, (Object)object2), (Object)new db$set_element_fields$fn__13148(iSeq));
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return db$set_element_fields.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}


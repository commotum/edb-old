/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import datomic.impl.db.IDatum;

public final class db$assertion_policy_compare
extends AFunction {
    public static final Object const__1 = 0L;
    public static final Object const__2 = -1L;
    public static final Keyword const__3 = RT.keyword(null, (String)"else");
    public static final Object const__4 = 1L;

    public static Object invokeStatic(Object a, Object b) {
        Object object;
        Object object2 = b;
        b = null;
        if (((IDatum)a).isAssertion() == ((IDatum)object2).isAssertion()) {
            object = const__1;
        } else {
            Object object3 = a;
            a = null;
            if (((IDatum)object3).isAssertion()) {
                object = const__2;
            } else {
                Keyword keyword = const__3;
                object = keyword != null && keyword != Boolean.FALSE ? const__4 : null;
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$assertion_policy_compare.invokeStatic(object3, object4);
    }
}


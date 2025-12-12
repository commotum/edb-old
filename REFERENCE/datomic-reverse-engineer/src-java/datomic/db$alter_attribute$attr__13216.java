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
import datomic.Database;

public final class db$alter_attribute$attr__13216
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"disabled");

    public Object invoke(Object db2, Object ent, Object id) {
        Object object;
        Object temp__5455__auto__13218;
        Object object2 = ent;
        ent = null;
        Object object3 = db2;
        db2 = null;
        Object object4 = id;
        id = null;
        Object object5 = temp__5455__auto__13218 = RT.get((Object)object2, (Object)((Database)object3).ident(object4));
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = temp__5455__auto__13218;
            temp__5455__auto__13218 = null;
            Object e = object6;
            object = e;
            e = null;
        } else {
            object = const__1;
        }
        return object;
    }
}


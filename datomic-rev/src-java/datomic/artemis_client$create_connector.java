/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class artemis_client$create_connector
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"datomic.artemis-client", (String)"create-transport");

    public static Object invokeStatic(Object connector_class_name, ISeq kvs) {
        Object object = connector_class_name;
        connector_class_name = null;
        ISeq iSeq = kvs;
        kvs = null;
        return ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object, (Object)iSeq);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return artemis_client$create_connector.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}


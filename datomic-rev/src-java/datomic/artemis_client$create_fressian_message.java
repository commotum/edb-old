/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  org.fressian.Writer
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import org.fressian.Writer;

public final class artemis_client$create_fressian_message
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"datomic.artemis-client", (String)"create-message");
    public static final Var const__2 = RT.var((String)"datomic.fressian", (String)"create-writer");
    public static final Var const__3 = RT.var((String)"datomic.artemis-client", (String)"output-stream");

    public static Object invokeStatic(Object session, Object lookup, Object obj, ISeq message_args) {
        Object fout;
        Object object = session;
        session = null;
        ISeq iSeq = message_args;
        message_args = null;
        Object msg = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), object, (Object)iSeq);
        Object object2 = lookup;
        lookup = null;
        Object object3 = fout = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(msg), object2);
        fout = null;
        Object object4 = obj;
        obj = null;
        ((Writer)object3).writeObject(object4);
        Object object5 = msg;
        msg = null;
        return object5;
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return artemis_client$create_fressian_message.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}


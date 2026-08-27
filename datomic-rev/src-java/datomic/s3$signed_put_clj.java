/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import datomic.s3$signed_put_clj$fn__23314;
import datomic.s3$signed_put_clj$fn__23316;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public final class s3$signed_put_clj
extends AFunction {
    public static Object invokeStatic(Object url, Object obj) {
        InputStream is;
        Object object = url;
        url = null;
        URLConnection conn = ((URL)object).openConnection();
        conn.setDoOutput(Boolean.TRUE);
        ((HttpURLConnection)conn).setRequestMethod("PUT");
        OutputStream os = conn.getOutputStream();
        Object object2 = obj;
        obj = null;
        OutputStream outputStream = os;
        os = null;
        ((IFn)new s3$signed_put_clj$fn__23314(object2, outputStream)).invoke();
        URLConnection uRLConnection = conn;
        conn = null;
        InputStream inputStream = is = uRLConnection.getInputStream();
        is = null;
        ((IFn)new s3$signed_put_clj$fn__23316(inputStream)).invoke();
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return s3$signed_put_clj.invokeStatic(object3, object4);
    }
}


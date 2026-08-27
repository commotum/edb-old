/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.InputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.net.URL;

public final class s3$signed_get_clj
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"read");
    public static final Var const__1 = RT.var((String)"clojure.java.io", (String)"reader");

    public static Object invokeStatic(Object url) {
        Object object;
        Object object2 = url;
        url = null;
        InputStream is = ((URL)object2).openStream();
        try {
            object = ((IFn)const__0.getRawRoot()).invoke((Object)new PushbackReader((Reader)((IFn)const__1.getRawRoot()).invoke((Object)is)));
        }
        finally {
            InputStream inputStream = is;
            is = null;
            inputStream.close();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return s3$signed_get_clj.invokeStatic(object2);
    }
}


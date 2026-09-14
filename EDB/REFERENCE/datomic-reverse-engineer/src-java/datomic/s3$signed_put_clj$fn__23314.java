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
import java.io.OutputStream;
import java.io.Writer;

public final class s3$signed_put_clj$fn__23314
extends AFunction {
    Object obj;
    Object os;
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"writer");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"*out*");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"pr");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public s3$signed_put_clj$fn__23314(Object object, Object object2) {
        this.obj = object;
        this.os = object2;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            Object writer2 = ((IFn)const__0.getRawRoot()).invoke(this.os);
            try {
                Object object3;
                ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__3, writer2));
                try {
                    this.obj = null;
                    object3 = ((IFn)const__4.get()).invoke(this.obj);
                }
                finally {
                    ((IFn)const__5.getRawRoot()).invoke();
                }
                object2 = object3;
            }
            finally {
                Object object4 = writer2;
                writer2 = null;
                ((Writer)object4).close();
            }
            object = object2;
        }
        finally {
            this.os = null;
            ((OutputStream)this.os).close();
        }
        return object;
    }
}


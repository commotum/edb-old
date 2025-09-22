/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Reader
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.InputStream;
import org.fressian.Reader;

public final class fressian$read_seq$fn__12187
extends AFunction {
    Object readable;
    Object fill;
    Object handler_lookup;
    Object done;
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"input-stream");
    public static final Var const__1 = RT.var((String)"datomic.fressian", (String)"create-reader");

    public fressian$read_seq$fn__12187(Object object, Object object2, Object object3, Object object4) {
        this.readable = object;
        this.fill = object2;
        this.handler_lookup = object3;
        this.done = object4;
    }

    public Object invoke() {
        Object object;
        try {
            Object object2;
            this.readable = null;
            Object s = ((IFn)const__0.getRawRoot()).invoke(this.readable);
            try {
                this.handler_lookup = null;
                Object f = ((IFn)const__1.getRawRoot()).invoke(s, this.handler_lookup);
                while ((long)((InputStream)s).available() != 0L) {
                    ((IFn)this.fill).invoke(((Reader)f).readObject());
                }
                object2 = ((IFn)this.done).invoke();
            }
            finally {
                Object object3 = s;
                s = null;
                ((InputStream)object3).close();
            }
            object = object2;
        }
        catch (Throwable t2) {
            this.fill = null;
            Object t2 = null;
            object = ((IFn)this.fill).invoke((Object)t2);
        }
        return object;
    }
}


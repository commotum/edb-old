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
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class io$byte_buffer_seq$fn__9350
extends AFunction {
    Object bb;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__1 = RT.var((String)"datomic.io", (String)"byte-buffer-seq");

    public io$byte_buffer_seq$fn__9350(Object object) {
        this.bb = object;
    }

    public Object invoke() {
        Object object;
        if (((Buffer)this_.bb).hasRemaining()) {
            this_.bb = null;
            ByteBuffer next_slice = ((ByteBuffer)this_.bb).slice();
            Byte by = next_slice.get();
            ByteBuffer byteBuffer = next_slice;
            next_slice = null;
            io$byte_buffer_seq$fn__9350 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)by, ((IFn)const__1.getRawRoot()).invoke((Object)byteBuffer));
        } else {
            object = null;
        }
        return object;
    }
}


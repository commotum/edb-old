/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  org.fressian.impl.Codes
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import org.fressian.impl.Codes;

public final class log$resets_caches_QMARK_
extends AFunction {
    public static Object invokeStatic(Object bbuf) {
        ByteBuffer byteBuffer = (ByteBuffer)bbuf;
        Object object = bbuf;
        bbuf = null;
        return Util.equiv((long)RT.longCast((Object)RT.uncheckedByteCast((int)Codes.RESET_CACHES)), (long)RT.longCast((Object)byteBuffer.get(((Buffer)object).position()))) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$resets_caches_QMARK_.invokeStatic(object2);
    }
}


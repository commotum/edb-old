/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class valcache$scan_strings
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object bb) {
        Object strs = PersistentVector.EMPTY;
        StringBuffer buf = new StringBuffer();
        while (((Buffer)bb).hasRemaining()) {
            byte ch = ((ByteBuffer)bb).get();
            if (RT.longCast((Object)ch) == 0L) {
                if ((long)RT.count((Object)buf) == 0L) {
                    PersistentVector persistentVector = strs;
                    strs = null;
                    StringBuffer stringBuffer = buf;
                    buf = null;
                    buf = stringBuffer;
                    strs = persistentVector;
                    continue;
                }
                PersistentVector persistentVector = strs;
                strs = null;
                StringBuffer stringBuffer = buf;
                buf = null;
                buf = new StringBuffer();
                strs = ((IFn)const__2.getRawRoot()).invoke((Object)persistentVector, ((IFn)const__3.getRawRoot()).invoke((Object)stringBuffer));
                continue;
            }
            PersistentVector persistentVector = strs;
            strs = null;
            StringBuffer stringBuffer = buf;
            buf = null;
            buf = stringBuffer.append(RT.charCast((byte)ch));
            strs = persistentVector;
        }
        PersistentVector persistentVector = strs;
        strs = null;
        StringBuffer stringBuffer = buf;
        buf = null;
        return ((IFn)const__2.getRawRoot()).invoke((Object)persistentVector, ((IFn)const__3.getRawRoot()).invoke((Object)stringBuffer));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return valcache$scan_strings.invokeStatic(object2);
    }
}


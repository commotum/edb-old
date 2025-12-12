/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class valcache$direct_get
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"root");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"k");
    public static final Var const__4 = RT.var((String)"datomic.valcache", (String)"full-path");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__7 = RT.classForName((String)"java.nio.file.OpenOption");
    public static final Var const__12 = RT.var((String)"datomic.io", (String)"read-into-buffer");

    public static Object invokeStatic(Object root, Object k) {
        Object object;
        Boolean bl;
        File G__9818;
        File file;
        Object path2;
        Object object2 = root;
        if (object2 == null || object2 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__2))));
        }
        Object object3 = k;
        if (object3 == null || object3 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__0.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__1.getRawRoot()).invoke((Object)const__3))));
        }
        Object object4 = root;
        root = null;
        Object object5 = k;
        k = null;
        Object G__98182 = path2 = ((IFn)const__4.getRawRoot()).invoke(object4, object5);
        if (Util.identical((Object)G__98182, null)) {
            file = null;
        } else {
            G__98182 = null;
            file = G__9818 = ((Path)G__98182).toFile();
        }
        if (Util.identical((Object)G__9818, null)) {
            bl = null;
        } else {
            File file2 = G__9818;
            G__9818 = null;
            bl = file2.exists() ? Boolean.TRUE : Boolean.FALSE;
        }
        if (bl != null && bl != Boolean.FALSE) {
            Object object6;
            Object object7 = path2;
            path2 = null;
            FileChannel fc = FileChannel.open((Path)object7, (OpenOption[])((IFn)const__6.getRawRoot()).invoke(const__7, (Object)Tuple.create((Object)StandardOpenOption.READ)));
            try {
                Object object8;
                long size = Numbers.minus((long)fc.size(), (long)4L);
                if (size > 0L) {
                    fc.position(4L);
                    object8 = ((IFn)const__12.getRawRoot()).invoke((Object)ByteBuffer.allocate(RT.intCast((long)size)), (Object)Numbers.num((long)size), (Object)fc);
                } else {
                    object8 = null;
                }
                object6 = object8;
            }
            finally {
                FileChannel fileChannel = fc;
                fc = null;
                ((AbstractInterruptibleChannel)fileChannel).close();
            }
            object = object6;
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$direct_get.invokeStatic(object3, object4);
    }
}


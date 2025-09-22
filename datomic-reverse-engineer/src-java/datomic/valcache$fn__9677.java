/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class valcache$fn__9677
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"opcode");
    public static final Keyword const__4 = RT.keyword(null, (String)"key-length");
    public static final Keyword const__5 = RT.keyword(null, (String)"extras-length");
    public static final Keyword const__6 = RT.keyword(null, (String)"total-body-length");
    public static final Keyword const__7 = RT.keyword(null, (String)"root");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__12 = RT.var((String)"datomic.io", (String)"read-n-bytes");
    public static final Var const__13 = RT.var((String)"datomic.valcache", (String)"reply-with-error");
    public static final Object const__14 = 4L;
    public static final Var const__15 = RT.var((String)"datomic.valcache", (String)"read-key");
    public static final Var const__16 = RT.var((String)"datomic.valcache", (String)"full-path");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__19 = RT.classForName((String)"java.nio.file.OpenOption");
    public static final Var const__21 = RT.var((String)"datomic.io", (String)"write-buffer");
    public static final Object const__24 = 0L;
    public static final Object const__25 = 1L;

    public static Object invokeStatic(Object p__9676, Object sc) {
        Object object;
        Boolean bl;
        Object object2;
        Object object3 = p__9676;
        p__9676 = null;
        Object map__9678 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__9678);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__9678;
            map__9678 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__9678;
            map__9678 = null;
        }
        Object map__96782 = object2;
        Object opcode = RT.get((Object)map__96782, (Object)const__3);
        Object key_length = RT.get((Object)map__96782, (Object)const__4);
        Object extras_length = RT.get((Object)map__96782, (Object)const__5);
        Object total_body_length = RT.get((Object)map__96782, (Object)const__6);
        Object object6 = map__96782;
        map__96782 = null;
        Object root = RT.get((Object)object6, (Object)const__7);
        IFn iFn = (IFn)const__8.getRawRoot();
        boolean and__5236__auto__9682 = Numbers.isPos((Object)key_length);
        if (and__5236__auto__9682) {
            Object object7 = extras_length;
            extras_length = null;
            boolean and__5236__auto__9681 = Numbers.isZero((Object)object7);
            bl = and__5236__auto__9681 ? (Util.equiv((Object)total_body_length, (Object)key_length) ? Boolean.TRUE : Boolean.FALSE) : (and__5236__auto__9681 ? Boolean.TRUE : Boolean.FALSE);
        } else {
            bl = and__5236__auto__9682 ? Boolean.TRUE : Boolean.FALSE;
        }
        Object object8 = iFn.invoke((Object)bl);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = total_body_length;
            total_body_length = null;
            ((IFn)const__12.getRawRoot()).invoke(object9, sc);
            Object object10 = opcode;
            opcode = null;
            Object object11 = sc;
            sc = null;
            object = ((IFn)const__13.getRawRoot()).invoke(object10, const__14, (Object)"Invalid args", object11);
        } else {
            Boolean bl2;
            File G__9679;
            File file;
            Object object12 = key_length;
            key_length = null;
            Object k = ((IFn)const__15.getRawRoot()).invoke(object12, sc);
            Object object13 = root;
            root = null;
            Object object14 = k;
            k = null;
            Object path2 = ((IFn)const__16.getRawRoot()).invoke(object13, object14);
            Object G__96792 = path2;
            if (Util.identical((Object)G__96792, null)) {
                file = null;
            } else {
                G__96792 = null;
                file = G__9679 = ((Path)G__96792).toFile();
            }
            if (Util.identical((Object)G__9679, null)) {
                bl2 = null;
            } else {
                File file2 = G__9679;
                G__9679 = null;
                bl2 = file2.exists() ? Boolean.TRUE : Boolean.FALSE;
            }
            if (bl2 != null && bl2 != Boolean.FALSE) {
                Object var12_14;
                Object object15 = path2;
                path2 = null;
                FileChannel fc = FileChannel.open((Path)object15, (OpenOption[])((IFn)const__18.getRawRoot()).invoke(const__19, (Object)Tuple.create((Object)StandardOpenOption.READ)));
                try {
                    ByteBuffer bb = ByteBuffer.allocate(RT.intCast((long)24L));
                    long size = fc.size();
                    ByteBuffer byteBuffer = bb;
                    bb = null;
                    Object object16 = opcode;
                    opcode = null;
                    ((IFn)const__21.getRawRoot()).invoke((Object)byteBuffer.put(RT.byteCast((long)-127L)).put(RT.byteCast((Object)object16)).putShort(RT.shortCast((Object)((Number)const__24))).put(RT.byteCast((long)4L)).put(RT.byteCast((long)0L)).putShort(RT.shortCast((Object)((Number)const__24))).putInt(RT.intCast((long)size)).putInt(RT.intCast((long)0L)).putLong(0L).flip(), sc);
                    Object object17 = sc;
                    sc = null;
                    long n = fc.transferTo(0L, size, (WritableByteChannel)object17);
                    var12_14 = null;
                }
                finally {
                    FileChannel fileChannel = fc;
                    fc = null;
                    ((AbstractInterruptibleChannel)fileChannel).close();
                }
                object = var12_14;
            } else {
                Object object18 = opcode;
                opcode = null;
                Object object19 = sc;
                sc = null;
                object = ((IFn)const__13.getRawRoot()).invoke(object18, const__25, (Object)"Not found", object19);
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$fn__9677.invokeStatic(object3, object4);
    }
}


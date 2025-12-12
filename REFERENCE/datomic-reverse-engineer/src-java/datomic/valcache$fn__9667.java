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
import clojure.lang.Var;
import datomic.valcache$fn__9667$fn__9669;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public final class valcache$fn__9667
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"opcode");
    public static final Keyword const__4 = RT.keyword(null, (String)"key-length");
    public static final Keyword const__5 = RT.keyword(null, (String)"extras-length");
    public static final Keyword const__6 = RT.keyword(null, (String)"total-body-length");
    public static final Keyword const__7 = RT.keyword(null, (String)"root");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"not=");
    public static final Object const__10 = 8L;
    public static final Var const__13 = RT.var((String)"datomic.io", (String)"read-n-bytes");
    public static final Var const__14 = RT.var((String)"datomic.valcache", (String)"reply-with-error");
    public static final Object const__15 = 4L;
    public static final Var const__16 = RT.var((String)"datomic.io", (String)"read-into-buffer");
    public static final Var const__18 = RT.var((String)"datomic.valcache", (String)"full-path");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"not");
    public static final Keyword const__22 = RT.keyword(null, (String)"else");
    public static final Object const__23 = 131L;
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__25 = RT.classForName((String)"java.nio.file.OpenOption");
    public static final Object const__26 = RT.classForName((String)"java.nio.file.CopyOption");
    public static final Var const__27 = RT.var((String)"datomic.valcache", (String)"reply-empty-ok");

    public static Object invokeStatic(Object p__9666, Object sc) {
        Object object;
        Object object2;
        Object object3;
        Object object4 = p__9666;
        p__9666 = null;
        Object map__9668 = object4;
        Object object5 = ((IFn)const__0.getRawRoot()).invoke(map__9668);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__9668;
            map__9668 = null;
            object3 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object6)));
        } else {
            object3 = map__9668;
            map__9668 = null;
        }
        Object map__96682 = object3;
        Object opcode = RT.get((Object)map__96682, (Object)const__3);
        Object key_length = RT.get((Object)map__96682, (Object)const__4);
        Object extras_length = RT.get((Object)map__96682, (Object)const__5);
        Object total_body_length = RT.get((Object)map__96682, (Object)const__6);
        Object object7 = map__96682;
        map__96682 = null;
        Object root = RT.get((Object)object7, (Object)const__7);
        boolean or__5238__auto__9673 = Numbers.isZero((Object)key_length);
        if (or__5238__auto__9673) {
            object2 = or__5238__auto__9673 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object or__5238__auto__9672;
            Object object8 = or__5238__auto__9672 = ((IFn)const__9.getRawRoot()).invoke(extras_length, const__10);
            if (object8 != null && object8 != Boolean.FALSE) {
                object2 = or__5238__auto__9672;
                or__5238__auto__9672 = null;
            } else {
                object2 = Numbers.lte((Object)total_body_length, (Object)Numbers.add((Object)key_length, (Object)extras_length)) ? Boolean.TRUE : Boolean.FALSE;
            }
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object9 = total_body_length;
            total_body_length = null;
            ((IFn)const__13.getRawRoot()).invoke(object9, sc);
            Object object10 = opcode;
            opcode = null;
            Object object11 = sc;
            sc = null;
            object = ((IFn)const__14.getRawRoot()).invoke(object10, const__15, (Object)"Invalid args", object11);
        } else {
            String k;
            Object object12 = extras_length;
            extras_length = null;
            Number kelen = Numbers.add((Object)key_length, (Object)object12);
            ByteBuffer eb = ByteBuffer.allocate(RT.intCast((Object)kelen));
            ((IFn)const__16.getRawRoot()).invoke((Object)eb, (Object)kelen, sc);
            int flags = eb.getInt();
            int expiry = eb.getInt();
            Object object13 = key_length;
            key_length = null;
            byte[] kbytes = Numbers.byte_array((Object)object13);
            ByteBuffer byteBuffer = eb;
            eb = null;
            byteBuffer.get(kbytes);
            byte[] byArray = kbytes;
            kbytes = null;
            String string = k = new String(byArray, "UTF-8");
            k = null;
            Object path2 = ((IFn)const__18.getRawRoot()).invoke(root, (Object)string);
            Object object14 = root;
            root = null;
            Object tmp_path = ((IFn)const__18.getRawRoot()).invoke(object14, ((IFn)const__19.getRawRoot()).invoke((Object)UUID.randomUUID()));
            Object object15 = total_body_length;
            total_body_length = null;
            Number number = kelen;
            kelen = null;
            Number vlen = Numbers.minus((Object)object15, (Object)number);
            boolean and__5236__auto__9674 = Numbers.isZero((long)expiry);
            Object object16 = ((IFn)const__21.getRawRoot()).invoke(and__5236__auto__9674 ? path2 : (and__5236__auto__9674 ? Boolean.TRUE : Boolean.FALSE));
            if (object16 != null && object16 != Boolean.FALSE) {
                String string2;
                Number number2 = vlen;
                vlen = null;
                ((IFn)const__13.getRawRoot()).invoke((Object)number2, sc);
                IFn iFn = (IFn)const__19.getRawRoot();
                Object object17 = ((IFn)const__21.getRawRoot()).invoke((Object)(Numbers.isZero((long)expiry) ? Boolean.TRUE : Boolean.FALSE));
                if (object17 != null && object17 != Boolean.FALSE) {
                    string2 = "expiry";
                } else {
                    Keyword keyword = const__22;
                    string2 = keyword != null && keyword != Boolean.FALSE ? "non-uuid key" : null;
                }
                Object s = iFn.invoke((Object)"Unsupported: ", (Object)string2);
                Object object18 = opcode;
                opcode = null;
                Object object19 = s;
                s = null;
                Object object20 = sc;
                sc = null;
                object = ((IFn)const__14.getRawRoot()).invoke(object18, const__23, object19, object20);
            } else {
                FileChannel fc = FileChannel.open((Path)tmp_path, (OpenOption[])((IFn)const__24.getRawRoot()).invoke(const__25, (Object)Tuple.create((Object)StandardOpenOption.CREATE, (Object)StandardOpenOption.WRITE, (Object)StandardOpenOption.TRUNCATE_EXISTING)));
                Number number3 = vlen;
                vlen = null;
                FileChannel fileChannel = fc;
                fc = null;
                ((IFn)new valcache$fn__9667$fn__9669(sc, flags, number3, fileChannel)).invoke();
                Object object21 = tmp_path;
                tmp_path = null;
                Object object22 = path2;
                path2 = null;
                Files.move((Path)object21, (Path)object22, (CopyOption[])((IFn)const__24.getRawRoot()).invoke(const__26, (Object)Tuple.create((Object)StandardCopyOption.REPLACE_EXISTING)));
                Object object23 = opcode;
                opcode = null;
                Object object24 = sc;
                sc = null;
                object = ((IFn)const__27.getRawRoot()).invoke(object23, object24);
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return valcache$fn__9667.invokeStatic(object3, object4);
    }
}


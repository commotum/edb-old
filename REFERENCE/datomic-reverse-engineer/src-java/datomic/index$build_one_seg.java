/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.fressian.Writer
 *  org.fressian.impl.BytesOutputStream
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$build_one_seg$f__15304__auto____15319;
import datomic.index$build_one_seg$proc__15317;
import datomic.index.TransposedData;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import org.fressian.Writer;
import org.fressian.impl.BytesOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class index$build_one_seg
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"*pace-index-fn*");
    public static final Object const__1 = 1L;
    public static final Var const__3 = RT.var((String)"datomic.fressian", (String)"create-writer");
    public static final Var const__5 = RT.var((String)"datomic.fressian", (String)"begin-closed-list");
    public static final Object const__6 = 0L;
    public static final Var const__9 = RT.var((String)"datomic.fressian", (String)"end-list");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"take");
    public static final Var const__12 = RT.var((String)"datomic.index", (String)"transpose");
    public static final Var const__13 = RT.var((String)"datomic.io", (String)"bytestream->buf");
    public static final Var const__14 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__15 = RT.keyword(null, (String)"event");
    public static final AFn const__16 = (AFn)Symbol.intern((String)"index", (String)"build-one-seg");
    public static final Keyword const__17 = RT.keyword(null, (String)"cnt");
    public static final Keyword const__18 = RT.keyword(null, (String)"written");
    public static final Keyword const__19 = RT.keyword(null, (String)"length");
    public static final Keyword const__20 = RT.keyword(null, (String)"bpd");

    public static Object invokeStatic(Object data2, Object cnt, Object write_handlers2) {
        IPersistentVector iPersistentVector;
        Object temp__5457__auto__15329;
        Object object = temp__5457__auto__15329 = const__0.get();
        if (object != null && object != Boolean.FALSE) {
            Object f;
            Object object2 = temp__5457__auto__15329;
            temp__5457__auto__15329 = null;
            Object object3 = f = object2;
            f = null;
            ((IFn)object3).invoke(const__1);
        }
        BytesOutputStream bos = new BytesOutputStream();
        try {
            IPersistentVector iPersistentVector2;
            GZIPOutputStream gz = new GZIPOutputStream((OutputStream)bos);
            try {
                IPersistentVector iPersistentVector3;
                BufferedOutputStream bs = new BufferedOutputStream(gz);
                try {
                    index$build_one_seg$f__15304__auto____15319 f__15304__auto__15330;
                    index$build_one_seg$proc__15317 proc;
                    Object data_start = data2;
                    long eatover = 3L;
                    Object object4 = write_handlers2;
                    write_handlers2 = null;
                    Object w = ((IFn)const__3.getRawRoot()).invoke((Object)bs, object4);
                    ((Writer)w).writeTag((Object)"index-tdata", RT.uncheckedIntCast((long)5L));
                    ((IFn)const__5.getRawRoot()).invoke(w);
                    index$build_one_seg$proc__15317 index$build_one_seg$proc__15317 = proc = new index$build_one_seg$proc__15317(w);
                    proc = null;
                    index$build_one_seg$f__15304__auto____15319 index$build_one_seg$f__15304__auto____15319 = f__15304__auto__15330 = new index$build_one_seg$f__15304__auto____15319(bos, (Object)index$build_one_seg$proc__15317, eatover);
                    f__15304__auto__15330 = null;
                    Object object5 = data2;
                    data2 = null;
                    Object object6 = cnt;
                    cnt = null;
                    Object vec__15314 = ((IFn)index$build_one_seg$f__15304__auto____15319).invoke(object5, object6, const__6, null, null);
                    Object data3 = RT.nth((Object)vec__15314, (int)RT.uncheckedIntCast((long)0L), null);
                    Object cnt2 = RT.nth((Object)vec__15314, (int)RT.uncheckedIntCast((long)1L), null);
                    Object object7 = vec__15314;
                    vec__15314 = null;
                    Object written = RT.nth((Object)object7, (int)RT.uncheckedIntCast((long)2L), null);
                    ((IFn)const__9.getRawRoot()).invoke(w);
                    Object object8 = data_start;
                    data_start = null;
                    Object dvec = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__11.getRawRoot()).invoke(written, object8));
                    Object d = ((IFn)const__12.getRawRoot()).invoke(dvec);
                    ((Writer)w).writeObject(((TransposedData)d).getEs());
                    ((Writer)w).writeObject(((TransposedData)d).getAs());
                    ((Writer)w).writeObject(((TransposedData)d).ts);
                    ((Writer)w).writeObject(((TransposedData)d).ops);
                    Object object9 = w;
                    w = null;
                    ((Writer)object9).writeFooter();
                    bs.flush();
                    gz.finish();
                    Object buf = ((IFn)const__13.getRawRoot()).invoke((Object)bos);
                    Logger logger = LoggerFactory.getLogger((String)"datomic.index");
                    if (logger.isDebugEnabled()) {
                        Logger logger2 = logger;
                        logger = null;
                        Object[] objectArray = new Object[10];
                        objectArray[0] = const__15;
                        objectArray[1] = const__16;
                        objectArray[2] = const__17;
                        Object object10 = cnt2;
                        cnt2 = null;
                        objectArray[3] = object10;
                        objectArray[4] = const__18;
                        objectArray[5] = written;
                        objectArray[6] = const__19;
                        objectArray[7] = ((Buffer)buf).remaining();
                        objectArray[8] = const__20;
                        objectArray[9] = Numbers.quotient((long)((Buffer)buf).remaining(), (Object)written);
                        logger2.debug((String)((IFn)const__14.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                    }
                    Object object11 = buf;
                    buf = null;
                    Object object12 = data3;
                    data3 = null;
                    Object object13 = written;
                    written = null;
                    Object object14 = dvec;
                    Object object15 = dvec;
                    dvec = null;
                    Object object16 = d;
                    d = null;
                    iPersistentVector3 = Tuple.create((Object)object11, (Object)object12, (Object)object13, (Object)RT.nth((Object)object14, (int)RT.uncheckedIntCast((long)((long)RT.count((Object)object15) - 1L))), (Object)object16);
                }
                finally {
                    BufferedOutputStream bufferedOutputStream = bs;
                    bs = null;
                    ((FilterOutputStream)bufferedOutputStream).close();
                }
                iPersistentVector2 = iPersistentVector3;
            }
            finally {
                GZIPOutputStream gZIPOutputStream = gz;
                gz = null;
                ((DeflaterOutputStream)gZIPOutputStream).close();
            }
            iPersistentVector = iPersistentVector2;
        }
        finally {
            BytesOutputStream bytesOutputStream = bos;
            bos = null;
            ((ByteArrayOutputStream)bytesOutputStream).close();
        }
        return iPersistentVector;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return index$build_one_seg.invokeStatic(object4, object5, object6);
    }
}


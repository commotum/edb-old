/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.fressian.impl.BytesOutputStream
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.index$build_one_seg$f__15304__auto____15319$f__15304__auto____15320;
import org.fressian.impl.BytesOutputStream;

public final class index$build_one_seg$f__15304__auto____15319
extends AFunction {
    Object bos;
    Object proc;
    long eatover;
    public static final Object const__1 = 0L;
    public static final Keyword const__9 = RT.keyword(null, (String)"else");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"next");

    public index$build_one_seg$f__15304__auto____15319(Object object, Object object2, long l) {
        this.bos = object;
        this.proc = object2;
        this.eatover = l;
    }

    public Object invoke(Object data2, Object cnt, Object i, Object over, Object prev) {
        Object object;
        block4: {
            while (true) {
                if (Util.identical((Object)data2, null)) {
                    Object object2 = i;
                    i = null;
                    object = Tuple.create(null, (Object)const__1, (Object)object2);
                    break block4;
                }
                boolean and__5236__auto__15326 = Util.identical((Object)over, null);
                if (and__5236__auto__15326 ? Numbers.gt((Object)Numbers.unchecked_add((long)((BytesOutputStream)this_.bos).length(), (Object)Numbers.unchecked_multiply((long)this_.eatover, (Object)i)), (long)16000L) : and__5236__auto__15326) {
                    index$build_one_seg$f__15304__auto____15319$f__15304__auto____15320 f__15304__auto__15327;
                    if (Numbers.lt((Object)Numbers.unchecked_multiply((Object)Numbers.unchecked_add((Object)Numbers.divide((long)((BytesOutputStream)this_.bos).length(), (Object)i), (long)this_.eatover), (Object)cnt), (double)Numbers.unchecked_multiply((double)0.5, (long)16000L))) {
                        Object object3 = data2;
                        data2 = null;
                        Object object4 = cnt;
                        cnt = null;
                        Object object5 = i;
                        i = null;
                        Object object6 = prev;
                        prev = null;
                        prev = object6;
                        over = Boolean.TRUE;
                        i = object5;
                        cnt = object4;
                        data2 = object3;
                        continue;
                    }
                    Object object7 = prev;
                    prev = null;
                    index$build_one_seg$f__15304__auto____15319$f__15304__auto____15320 index$build_one_seg$f__15304__auto____15319$f__15304__auto____15320 = f__15304__auto__15327 = new index$build_one_seg$f__15304__auto____15319$f__15304__auto____15320(this_.proc, object7);
                    f__15304__auto__15327 = null;
                    Object object8 = data2;
                    data2 = null;
                    Object object9 = cnt;
                    cnt = null;
                    Object object10 = i;
                    i = null;
                    index$build_one_seg$f__15304__auto____15319 this_ = null;
                    object = ((IFn)index$build_one_seg$f__15304__auto____15319$f__15304__auto____15320).invoke(object8, object9, object10);
                    break block4;
                }
                Keyword keyword = const__9;
                if (keyword == null || keyword == Boolean.FALSE) break;
                Object d = ((IFn)const__10.getRawRoot()).invoke(data2);
                ((IFn)this_.proc).invoke(d);
                Object object11 = data2;
                data2 = null;
                Object object12 = cnt;
                cnt = null;
                Object object13 = i;
                i = null;
                Object object14 = over;
                over = null;
                Object object15 = d;
                d = null;
                prev = object15;
                over = object14;
                i = Numbers.unchecked_inc((Object)object13);
                cnt = Numbers.unchecked_dec((Object)object12);
                data2 = ((IFn)const__11.getRawRoot()).invoke(object11);
            }
            object = null;
        }
        return object;
    }
}


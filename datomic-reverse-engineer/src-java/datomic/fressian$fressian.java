/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  org.fressian.Writer
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import java.io.OutputStream;
import org.fressian.Writer;

public final class fressian$fressian
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"handlers");
    public static final Keyword const__4 = RT.keyword(null, (String)"footer");
    public static final Var const__5 = RT.var((String)"clojure.java.io", (String)"output-stream");
    public static final Var const__6 = RT.var((String)"datomic.fressian", (String)"create-writer");

    public static Object invokeStatic(Object out, Object obj, ISeq p__12173) {
        Writer writer2;
        ISeq iSeq;
        ISeq iSeq2 = p__12173;
        p__12173 = null;
        ISeq map__12174 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__12174);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__12174;
            map__12174 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__12174;
            map__12174 = null;
        }
        ISeq map__121742 = iSeq;
        Object handlers = RT.get((Object)map__121742, (Object)const__3);
        ISeq iSeq4 = map__121742;
        map__121742 = null;
        Object footer = RT.get((Object)iSeq4, (Object)const__4);
        Object object2 = out;
        out = null;
        Object os = ((IFn)const__5.getRawRoot()).invoke(object2);
        try {
            Writer writer3;
            Object object3 = handlers;
            handlers = null;
            Object writer4 = ((IFn)const__6.getRawRoot()).invoke(os, object3);
            Object object4 = obj;
            obj = null;
            ((Writer)writer4).writeObject(object4);
            Object object5 = footer;
            footer = null;
            if (object5 != null && object5 != Boolean.FALSE) {
                Object object6 = writer4;
                writer4 = null;
                writer3 = ((Writer)object6).writeFooter();
            } else {
                writer3 = null;
            }
            writer2 = writer3;
        }
        finally {
            Object object7 = os;
            os = null;
            ((OutputStream)object7).close();
        }
        return writer2;
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return fressian$fressian.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}


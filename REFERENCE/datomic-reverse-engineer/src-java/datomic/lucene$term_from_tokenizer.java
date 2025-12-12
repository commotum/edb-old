/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  com.datomic.lucene.analysis.tokenattributes.CharTermAttributeImpl
 *  com.datomic.lucene.util.AttributeSource
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import com.datomic.lucene.analysis.tokenattributes.CharTermAttributeImpl;
import com.datomic.lucene.util.AttributeSource;

public final class lucene$term_from_tokenizer
extends AFunction {
    public static final Object const__0 = RT.classForName((String)"com.datomic.lucene.analysis.tokenattributes.TermAttribute");

    public static Object invokeStatic(Object t) {
        Object object = t;
        t = null;
        return ((CharTermAttributeImpl)((AttributeSource)object).getAttribute((Class)const__0)).term();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$term_from_tokenizer.invokeStatic(object2);
    }
}


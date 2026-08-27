/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datomic.lucene.analysis.ReusableAnalyzerBase
 *  com.datomic.lucene.analysis.TokenStream
 *  com.datomic.lucene.analysis.standard.StandardAnalyzer
 *  com.datomic.lucene.util.Version
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datomic.lucene.analysis.ReusableAnalyzerBase;
import com.datomic.lucene.analysis.TokenStream;
import com.datomic.lucene.analysis.standard.StandardAnalyzer;
import com.datomic.lucene.util.Version;
import java.io.Reader;
import java.io.StringReader;

public final class lucene$tokenize_terms
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__1 = RT.var((String)"datomic.lucene", (String)"term-from-tokenizer");

    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        TokenStream t = ((ReusableAnalyzerBase)new StandardAnalyzer(Version.LUCENE_33)).tokenStream("v", (Reader)new StringReader((String)object));
        Object terms = PersistentVector.EMPTY;
        while (t.incrementToken()) {
            PersistentVector persistentVector = terms;
            terms = null;
            terms = ((IFn)const__0.getRawRoot()).invoke((Object)persistentVector, ((IFn)const__1.getRawRoot()).invoke((Object)t));
        }
        Object var2_2 = null;
        return terms;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return lucene$tokenize_terms.invokeStatic(object2);
    }
}


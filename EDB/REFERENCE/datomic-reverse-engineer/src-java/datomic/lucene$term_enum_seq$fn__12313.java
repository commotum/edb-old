/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datomic.lucene.index.TermEnum
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datomic.lucene.index.TermEnum;

public final class lucene$term_enum_seq$fn__12313
extends AFunction {
    Object te;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"cons");
    public static final Var const__1 = RT.var((String)"datomic.lucene", (String)"term-enum-seq");

    public lucene$term_enum_seq$fn__12313(Object object) {
        this.te = object;
    }

    public Object invoke() {
        lucene$term_enum_seq$fn__12313 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)((TermEnum)this_.te).term(), ((IFn)const__1.getRawRoot()).invoke(this_.te));
    }
}


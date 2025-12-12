/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.val_store.fs;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

public final class FS$fn__21327
extends AFunction {
    Object new_file;
    Object doit;
    public static final Var const__0 = RT.var((String)"datomic.core2.val-store.fs", (String)"CREATE_DIRECTORIES_OPTS");

    public FS$fn__21327(Object object, Object object2) {
        this.new_file = object;
        this.doit = object2;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)this.doit).invoke();
        }
        catch (NoSuchFileException _) {
            Files.createDirectories(((Path)this.new_file).getParent(), (FileAttribute[])const__0.getRawRoot());
            object = ((IFn)this.doit).invoke();
        }
        return object;
    }
}


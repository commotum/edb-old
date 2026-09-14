(in-ns 'clojure.tools.decompiler)

(defn bc-for [classname->path]
  (fn [classname]
    (try
      (some-> classname
              (clojure.string/replace "." "/")
              classname->path
              absolute-filename
              clojure.tools.decompiler.bc/analyze-class)
      (catch org.apache.bcel.classfile.ClassFormatException _
        nil))))

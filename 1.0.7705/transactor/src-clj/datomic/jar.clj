(do
  (clojure.core/in-ns 'datomic.jar)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require ['clojure.java.io :as 'io])
      (clojure.core/import 'java.util.jar.JarEntry)
      (clojure.core/import 'java.util.jar.JarOutputStream)
      (clojure.core/import 'java.util.jar.Manifest)
      (clojure.core/import 'java.util.zip.ZipEntry)
      (clojure.core/import 'java.util.zip.ZipOutputStream)
      (clojure.core/import 'java.io.File)))
  (when-not (.equals 'datomic.jar 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.jar))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require ['clojure.java.io :as 'io])
        (clojure.core/import 'java.util.jar.JarEntry)
        (clojure.core/import 'java.util.jar.JarOutputStream)
        (clojure.core/import 'java.util.jar.Manifest)
        (clojure.core/import 'java.util.zip.ZipEntry)
        (clojure.core/import 'java.util.zip.ZipOutputStream)
        (clojure.core/import 'java.io.File))))
  (defn add-entries
    ([jos sources transform]
      (loop [seq_26572 (seq
                         (filter
                           (fn fn__26576 ([p1__26571#] (.isFile ^java.io.File p1__26571#)))
                           sources))
             chunk_26573 nil
             count_26574 0
             i_26575 0]
        (if (< i_26575 count_26574)
          (let [source (.nth ^clojure.lang.Indexed chunk_26573 (int i_26575))]
            (let [entry (java.util.jar.JarEntry.
                          (^clojure.lang.IFn transform (.getPath ^java.io.File source)))]
              (.setTime ^java.util.zip.ZipEntry entry (long (.lastModified ^java.io.File source)))
              (.putNextEntry ^java.util.jar.JarOutputStream jos ^java.util.zip.ZipEntry entry))
            (io/copy source jos)
            (recur seq_26572 chunk_26573 count_26574 (inc i_26575)))
          (let [temp__5825__auto__ (seq seq_26572)]
            (when temp__5825__auto__
              (let [seq_26572 temp__5825__auto__]
                (if (chunked-seq? seq_26572)
                  (let [c__6090__auto__ (chunk-first seq_26572)]
                    (recur
                      (chunk-rest seq_26572)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [source (first seq_26572)]
                    (let [entry (java.util.jar.JarEntry.
                                  (^clojure.lang.IFn transform (.getPath ^java.io.File source)))]
                      (.setTime
                        ^java.util.zip.ZipEntry entry
                        (long (.lastModified ^java.io.File source)))
                      (.putNextEntry
                        ^java.util.jar.JarOutputStream jos
                        ^java.util.zip.ZipEntry entry))
                    (io/copy source jos)
                    (recur (next seq_26572) nil 0 0))))))))))
  (reset-meta!
    #'add-entries
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'jos {:tag 'JarOutputStream}) 'sources 'transform]),
       :column (int 1)}
      :name
      'add-entries
      :ns
      *ns*))
  (defn create-jar
    ([archive f]
      (with-open [dest (io/output-stream (io/file archive))]
        (with-open [jos (java.util.jar.JarOutputStream.
                          ^java.io.OutputStream dest
                          (java.util.jar.Manifest.))]
          (^clojure.lang.IFn f jos)))))
  (reset-meta!
    #'create-jar
    (assoc
      {:arglists (clojure.core/list ['archive 'f]), :column (int 1)}
      :name
      'create-jar
      :ns
      *ns*))
  (defn add-zip-entries
    ([jos sources transform]
      (loop [seq_26583 (seq
                         (filter
                           (fn fn__26587 ([p1__26582#] (.isFile ^java.io.File p1__26582#)))
                           sources))
             chunk_26584 nil
             count_26585 0
             i_26586 0]
        (if (< i_26586 count_26585)
          (let [source (.nth ^clojure.lang.Indexed chunk_26584 (int i_26586))]
            (let [entry (java.util.zip.ZipEntry.
                          (^clojure.lang.IFn transform (.getPath ^java.io.File source)))]
              (.setTime ^java.util.zip.ZipEntry entry (long (.lastModified ^java.io.File source)))
              (.putNextEntry ^java.util.zip.ZipOutputStream jos ^java.util.zip.ZipEntry entry))
            (io/copy source jos)
            (recur seq_26583 chunk_26584 count_26585 (inc i_26586)))
          (let [temp__5825__auto__ (seq seq_26583)]
            (when temp__5825__auto__
              (let [seq_26583 temp__5825__auto__]
                (if (chunked-seq? seq_26583)
                  (let [c__6090__auto__ (chunk-first seq_26583)]
                    (recur
                      (chunk-rest seq_26583)
                      c__6090__auto__
                      (int (count c__6090__auto__))
                      (int 0)))
                  (let [source (first seq_26583)]
                    (let [entry (java.util.zip.ZipEntry.
                                  (^clojure.lang.IFn transform (.getPath ^java.io.File source)))]
                      (.setTime
                        ^java.util.zip.ZipEntry entry
                        (long (.lastModified ^java.io.File source)))
                      (.putNextEntry
                        ^java.util.zip.ZipOutputStream jos
                        ^java.util.zip.ZipEntry entry))
                    (io/copy source jos)
                    (recur (next seq_26583) nil 0 0))))))))))
  (reset-meta!
    #'add-zip-entries
    (assoc
      {:arglists
       (clojure.core/list [(.withMeta 'jos {:tag 'ZipOutputStream}) 'sources 'transform]),
       :column (int 1)}
      :name
      'add-zip-entries
      :ns
      *ns*))
  (defn create-zip
    ([archive f]
      (with-open [dest (io/output-stream (io/file archive))]
        (with-open [jos (java.util.zip.ZipOutputStream. ^java.io.OutputStream dest)]
          (^clojure.lang.IFn f jos)))))
  (reset-meta!
    #'create-zip
    (assoc
      {:arglists (clojure.core/list ['archive 'f]), :column (int 1)}
      :name
      'create-zip
      :ns
      *ns*)))
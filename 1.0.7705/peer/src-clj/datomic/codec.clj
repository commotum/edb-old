(do
  (clojure.core/in-ns 'datomic.codec)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'org.apache.commons.codec.binary.Base64)))
  (when-not (.equals 'datomic.codec 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.codec))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'org.apache.commons.codec.binary.Base64))))
  (defn string->bytes ([s] (.getBytes ^java.lang.String s "UTF-8")))
  (reset-meta!
    #'string->bytes
    (assoc
      {:arglists (clojure.core/list [(.withMeta 's {:tag 'String})]), :column (int 1)}
      :name
      'string->bytes
      :ns
      *ns*))
  (defn bytes->string ([b] (java.lang.String. ^bytes b "UTF-8")))
  (reset-meta!
    #'bytes->string
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'b {:tag 'bytes})]), :column (int 1)}
      :name
      'bytes->string
      :ns
      *ns*))
  (defn encode-64 ([raw] (Base64/encodeBase64 ^bytes raw (boolean (.booleanValue false)))))
  (reset-meta!
    #'encode-64
    (assoc
      {:arglists (clojure.core/list (.withMeta ['raw] {:tag 'bytes})), :column (int 1)}
      :name
      'encode-64
      :ns
      *ns*))
  (defn decode-64 ([coded] (Base64/decodeBase64 ^bytes coded)))
  (reset-meta!
    #'decode-64
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coded {:tag 'bytes})]), :column (int 1)}
      :name
      'decode-64
      :ns
      *ns*))
  (defn base-64-literal ([literal] (decode-64 (string->bytes literal))))
  (reset-meta!
    #'base-64-literal
    (assoc
      {:arglists (clojure.core/list ['literal]), :column (int 1)}
      :name
      'base-64-literal
      :ns
      *ns*)))
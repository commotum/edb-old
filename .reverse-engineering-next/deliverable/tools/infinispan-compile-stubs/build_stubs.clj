(require '[clojure.java.io :as io])
(import '[clojure.asm ClassWriter Opcodes])

(def output-root
  (or (first *command-line-args*)
      "/tmp/infinispan-5.1-compile-stubs/classes"))

(defn access [& flags]
  (reduce bit-or flags))

(defn write-class! [internal-name bytes]
  (let [file (io/file output-root (str internal-name ".class"))]
    (io/make-parents file)
    (with-open [out (io/output-stream file)]
      (.write out bytes))))

(defn abstract-method! [^ClassWriter writer name descriptor]
  (doto (.visitMethod writer
                      (access Opcodes/ACC_PUBLIC Opcodes/ACC_ABSTRACT)
                      name descriptor nil nil)
    (.visitEnd)))

(defn flag-bytes []
  (let [name "org/infinispan/client/hotrod/Flag"
        descriptor (str "L" name ";")
        writer (ClassWriter. ClassWriter/COMPUTE_MAXS)]
    (.visit writer Opcodes/V1_8
            (access Opcodes/ACC_PUBLIC Opcodes/ACC_FINAL Opcodes/ACC_SUPER)
            name nil "java/lang/Object" nil)
    (doto (.visitField writer
                       (access Opcodes/ACC_PUBLIC Opcodes/ACC_STATIC Opcodes/ACC_FINAL)
                       "FORCE_RETURN_VALUE" descriptor nil nil)
      (.visitEnd))
    (let [method (.visitMethod writer Opcodes/ACC_PRIVATE "<init>" "()V" nil nil)]
      (.visitCode method)
      (.visitVarInsn method Opcodes/ALOAD 0)
      (.visitMethodInsn method Opcodes/INVOKESPECIAL
                        "java/lang/Object" "<init>" "()V" false)
      (.visitInsn method Opcodes/RETURN)
      (.visitMaxs method 0 0)
      (.visitEnd method))
    (let [method (.visitMethod writer Opcodes/ACC_STATIC "<clinit>" "()V" nil nil)]
      (.visitCode method)
      (.visitTypeInsn method Opcodes/NEW name)
      (.visitInsn method Opcodes/DUP)
      (.visitMethodInsn method Opcodes/INVOKESPECIAL name "<init>" "()V" false)
      (.visitFieldInsn method Opcodes/PUTSTATIC name "FORCE_RETURN_VALUE" descriptor)
      (.visitInsn method Opcodes/RETURN)
      (.visitMaxs method 0 0)
      (.visitEnd method))
    (.visitEnd writer)
    (.toByteArray writer)))

(defn versioned-value-bytes []
  (let [name "org/infinispan/client/hotrod/VersionedValue"
        writer (ClassWriter. 0)]
    (.visit writer Opcodes/V1_8
            (access Opcodes/ACC_PUBLIC Opcodes/ACC_ABSTRACT Opcodes/ACC_INTERFACE)
            name nil "java/lang/Object" nil)
    (abstract-method! writer "getVersion" "()J")
    (abstract-method! writer "getValue" "()Ljava/lang/Object;")
    (.visitEnd writer)
    (.toByteArray writer)))

(defn remote-cache-bytes []
  (let [name "org/infinispan/client/hotrod/RemoteCache"
        writer (ClassWriter. 0)]
    (.visit writer Opcodes/V1_8
            (access Opcodes/ACC_PUBLIC Opcodes/ACC_ABSTRACT Opcodes/ACC_INTERFACE)
            name nil "java/lang/Object"
            (into-array String ["java/util/concurrent/ConcurrentMap"]))
    (abstract-method! writer
                      "withFlags"
                      "([Lorg/infinispan/client/hotrod/Flag;)Lorg/infinispan/client/hotrod/RemoteCache;")
    (abstract-method! writer
                      "getVersioned"
                      "(Ljava/lang/Object;)Lorg/infinispan/client/hotrod/VersionedValue;")
    (abstract-method! writer
                      "replaceWithVersion"
                      "(Ljava/lang/Object;Ljava/lang/Object;J)Z")
    (.visitEnd writer)
    (.toByteArray writer)))

(defn remote-cache-manager-bytes []
  (let [name "org/infinispan/client/hotrod/RemoteCacheManager"
        writer (ClassWriter. ClassWriter/COMPUTE_MAXS)]
    (.visit writer Opcodes/V1_8
            (access Opcodes/ACC_PUBLIC Opcodes/ACC_SUPER)
            name nil "java/lang/Object" nil)
    (let [method (.visitMethod writer Opcodes/ACC_PUBLIC
                               "<init>" "(Ljava/lang/String;I)V" nil nil)]
      (.visitCode method)
      (.visitVarInsn method Opcodes/ALOAD 0)
      (.visitMethodInsn method Opcodes/INVOKESPECIAL
                        "java/lang/Object" "<init>" "()V" false)
      (.visitInsn method Opcodes/RETURN)
      (.visitMaxs method 0 0)
      (.visitEnd method))
    (let [method (.visitMethod writer Opcodes/ACC_PUBLIC
                               "getCache"
                               "(Ljava/lang/String;)Lorg/infinispan/client/hotrod/RemoteCache;"
                               nil nil)]
      (.visitCode method)
      (.visitInsn method Opcodes/ACONST_NULL)
      (.visitInsn method Opcodes/ARETURN)
      (.visitMaxs method 0 0)
      (.visitEnd method))
    (.visitEnd writer)
    (.toByteArray writer)))

(doseq [[name bytes] [["org/infinispan/client/hotrod/Flag" (flag-bytes)]
                      ["org/infinispan/client/hotrod/VersionedValue" (versioned-value-bytes)]
                      ["org/infinispan/client/hotrod/RemoteCache" (remote-cache-bytes)]
                      ["org/infinispan/client/hotrod/RemoteCacheManager"
                       (remote-cache-manager-bytes)]]]
  (write-class! name bytes))

(println "wrote Infinispan 5.1 compile-only stubs to" output-root)

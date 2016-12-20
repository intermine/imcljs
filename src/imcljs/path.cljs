(ns imcljs.path
  (:require [imcljs.internal.utils :refer [does-not-contain?]]
            [clojure.string :refer [split join]]))

(defn split-path
  "Split a string path into a vector of keywords.
  (split-path `Gene.organism.shortName`)
  => [:Gene :organism :shortName]"
  [path-str] (map keyword (split path-str ".")))

(defn join-path
  "Join a vector path of keywords to a string.
  (join-path [:Gene :organism :shortName])
  => Gene.organism.shortName"
  [path-str] (join "." (map name path-str)))

(defn relationships
  "Given a model and a class, return its collections and references."
  [model class-kw]
  ;(.log js/console "RELS" (apply merge (map (get-in model [:classes class-kw]) [:references :collections])))
  (apply merge (map (get-in model [:classes class-kw]) [:attributes :references :collections]))
  ;(map (get-in model [:classes class-kw]) [:references :collections])
  )

(defn referenced-type
  "Given a model, a class, and a collection or reference, return the class of the collection or reference.
  (referenced-class im-model :Gene :homologues)
  => :Gene"
  [model field-kw class-kw]
  ;(.log js/console "referenced-type given" field-kw class-kw)
  ;(.log js/console "which produces" (keyword (:referencedType (get (relationships model class-kw) field-kw))))
  (keyword (:referencedType (get (relationships model class-kw) field-kw))))

(defn referenced-class
  "Given a model, a reference/collection, and a class,
  return the superclass of the reference/collection.
  In the example :tissue is an attribute of the subclass :FlyAtlasResult
  (referenced-class im-model :MicroArrayResult :tissue)
  => :Tissue"
  [model field-kw class-kw]
  (->> (:classes model)
       (filter (fn [[_ {:keys [extends]}]] (some (partial = class-kw) (map keyword extends))))
       (map first)
       (cons class-kw)
       (map (partial referenced-type model field-kw))
       (filter identity)
       first))

(defn referenced-values
  "Given a model, a class, and a collection or reference, return the class of the collection or reference.
  (referenced-class im-model :Gene :homologues)
  => :Gene"
  [model field-kw class-kw]
  (get (relationships model class-kw) field-kw))

(defn class-value
  "Given a model and a field, return that field from the data model.
  A field can be a reference, a collection, or an attribute
  In the example :tissue is an attribute of the subclass :FlyAtlasResult
  (referenced-class im-model :MicroArrayResult :tissue)
  => :Tissue"
  [model class-kw field-kw]
  (->> (:classes model)
       (filter (fn [[_ {:keys [extends]}]] (some (partial = class-kw) (map keyword extends))))
       (map first)
       (cons class-kw)
       (map (partial referenced-values model field-kw))
       (filter identity)
       first))

(defn walk
  "Return a vector representing each part of path.
  If any part of the path is unresolvable then a nil is returned.
  (walk im-model `Gene.organism.shortName`)
  => [{:name `Gene`, :collections {...}, :attributes {...}}
      {:name `Organism`, :collections {...} :attributes {...}
      {:name `shortName`, :type `java.lang.String`}]"
  ([model path]
   (walk model (if (string? path) (split-path path) path) []))
  ([model [class-kw & [path & remaining]] trail]
   (let [cv (class-value model class-kw path)]
     (if remaining
       (if (contains? cv :referencedType)
         (recur model
                (cons (keyword (:referencedType cv)) remaining)
                (conj trail (get-in model [:classes class-kw]))))
       (conj trail (get-in model [:classes class-kw]) cv)))))

(defn data-type
  "Return the java type of a path representing an attribute.
  (attribute-type im-model `Gene.organism.shortName`)
  => java.lang.String"
  [model path]
  (:type (last (walk model path))))

(defn class
  "Returns the class represented by the path.
  (class im-model `Gene.homologues.homologue.symbol`)
  => :Gene"
  [model path]
  (let [done (take-while #(does-not-contain? % :type) (walk model path))]
    (keyword (:name (last done)))))

(defn class?
  "Returns true if path is a class.
  (class im-model `Gene.diseases`)
  => true
  (class im-model `Gene.diseases.name`)
  => false"
  [model path]
  (let [walked (walk model path)]
    (not (contains? (last walked) :type))))

(defn trim-to-last-class
  "Returns a path string trimmed to the last class
  (trim-to-last-class im-model `Gene.homologues.homologue.symbol`)
  => Gene.homologues.homologue"
  [model path]
  (let [done (take-while #(does-not-contain? % :type) (walk model path))]
    (.log js/console "DONE" path (walk model path))
    (join-path (take (count done) (split-path path)))))

(defn friendly
  "Returns a path as a strong"
  ([model path & [exclude-root?]]
   (reduce
     (fn [total next]
       (str total (if total " > ") (or (:displayName next) (:name next))))
     nil
     (if exclude-root? (rest (walk model path)) (walk model path)))))
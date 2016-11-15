(ns imcljs.path
  (:require [imcljs.internal.utils :refer [does-not-contain?]]
            [clojure.string :refer [split join]]))

(def path-types
  {nil                 :class
   "java.lang.String"  :string
   "java.lang.Boolean" :boolean
   "java.lang.Integer" :integer
   "java.lang.Double"  :double
   "java.lang.Float"   :float})

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

(defn referenced-class
  "Given a model, a class, and a collection or reference, return the class of the collection or reference.
  (referenced-class im-model :Gene :homologues)
  => :Gene"
  [model class-kw field-kw]
  (keyword (:referencedType (get (apply merge (map (get-in model [:classes class-kw])[:references :collections])) field-kw))))

(defn is-attribute [class value]
  (get-in class [:attributes value]))

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
   (if-let [attribute (is-attribute (get-in model [:classes class-kw]) path)]
     (reduce conj trail [(get-in model [:classes class-kw]) attribute])
     (if-let [class (referenced-class model class-kw path)]
       (recur model (conj remaining class) (conj trail (get-in model [:classes class-kw])))
       (if-not path
         (if-let [final (get-in model [:classes class-kw])]
           (conj trail final)))))))

(defn data-type
  "Return the java type of a path representing an attribute.
  (attribute-type im-model `Gene.organism.shortName`)
  => java.lang.String"
  [model path]
  (if-let [walked (walk model path)]
    (get path-types (:type (last walked)))))

(defn class
  "Returns the class represented by the path.
  (class im-model `Gene.homologues.homologue.symbol`)
  => :Gene"
  [model path]
  (let [done (take-while #(does-not-contain? % :type) (walk model path))]
    (keyword (:name (last done)))))

(defn trim-to-last-class
  "Returns a path string trimmed to the last class
  (trim-to-last-class im-model `Gene.homologues.homologue.symbol`)
  => Gene.homologues.homologue"
  [model path]
  (let [done (take-while #(does-not-contain? % :type) (walk model path))]
    (join-path (take (count done) (split-path path)))))
(ns imcljs.path
  (:refer-clojure :exclude [class class?])
  (:require [imcljs.internal.utils :refer [does-not-contain?]]
            [clojure.string :refer [split join]]))

(defn split-path
  "Split a string path into a vector of keywords.
  (split-path `Gene.organism.shortName`)
  => [:Gene :organism :shortName]"
  [path-str] (mapv keyword (split path-str #"\.")))

(defn join-path
  "Join a vector path of keywords to a string.
  (join-path [:Gene :organism :shortName])
  => Gene.organism.shortName"
  [path-str] (join "." (map name path-str)))

(defn properties
  "Given a model and a class, return its attributes, references and collections."
  [model class-kw]
  (apply merge (map (get-in model [:classes class-kw]) [:attributes :references :collections])))

(defn referenced-type
  "Given a model, a class, and a collection or reference, return the class of the collection or reference.
  (referenced-class im-model :Gene :homologues)
  => :Gene"
  [model field-kw class-kw]
  ;(.log js/console "referenced-type given" field-kw class-kw)
  ;(.log js/console "which produces" (keyword (:referencedType (get (properties model class-kw) field-kw))))
  (keyword (:referencedType (get (properties model class-kw) field-kw))))

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
  (get (properties model class-kw) field-kw))

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

(defn- walk-get-property
  "Returns the `prop` property of a model `class`. Lenient to nil arguments."
  [class prop]
  (when (map? class)
    (some->> prop
             (get (apply merge
                         (map class [:attributes :references :collections]))))))

(defn- walk-current-path
  "Returns the portion of `path` that has been consumed by `rem-path`."
  [path rem-path]
  (subvec path 0 (- (count path)
                    (count rem-path))))

(defn- walk-loop
  [model path & {:keys [path->subclass walk-properties?]}]
  (let [root-class (get-in model [:classes (first path)])]
    (loop [rem-path (-> path next next) ; First iteration consumes two.
           property (walk-get-property root-class (second path))
           walked [root-class]]
      (let [next-class (when-let [reference (keyword (:referencedType property))]
                         ;; Use the subclass if defined for next-class.
                         (let [curr-path (walk-current-path path rem-path)
                               reference (get path->subclass curr-path reference)]
                           (get-in model [:classes reference])))
            walked (conj walked (if walk-properties?
                                  property ; Always use property if specified.
                                  (or next-class ; Otherwise use class.
                                      property)))] ; If the path ends at an attribute.
        (cond
          (empty? rem-path) walked
          (empty? property) nil ; Path is unresolvable.
          :else (recur (next rem-path)
                       (walk-get-property next-class (first rem-path))
                       walked))))))

(defn walk
  "Return a vector representing each part of path.
  If any part of the path is unresolvable then a nil is returned.
  (walk im-model `Gene.organism.shortName`)
  => [{:name `Gene`, :collections {...}, :attributes {...}}
      {:name `Organism`, :collections {...} :attributes {...}
      {:name `shortName`, :type `java.lang.String`}]
  If the path traverses a subclass, you'll need to add a `:type-constraints`
  key to `model` with a value like
      [{:path `Gene.interactions.participant2`, :type `Gene`}]
  for the path to be resolvable.
      (walk im-model-with-type-constraints
       `Gene.interactions.participant2.proteinAtlasExpression.tissue.name`)
  Optional keyword arguments:
  walk-properties? - If true, each element will be the parent's property,
  instead of the referencedType.  The first element will still be a class as it
  has no parent."
  [model path & {:keys [walk-properties?]}]
  (let [p (if (string? path) (split-path path) (mapv keyword path))]
    (if (= 1 (count p))
      [(get-in model [:classes (first p)])]
      (walk-loop model p
                 :path->subclass (->> (:type-constraints model)
                                      (filter #(contains? % :type)) ; In case there are other constraints there.
                                      (reduce (fn [m {:keys [path type]}]
                                                (assoc m (split-path path) (keyword type)))
                                              {}))
                 :walk-properties? walk-properties?))))

(defn data-type
  "Return the java type of a path representing an attribute.
  (attribute-type im-model `Gene.organism.shortName`)
  => java.lang.String
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  [model path]
  (:type (last (walk model path))))

(defn class
  "Returns the class represented by the path.
  (class im-model `Gene.homologues.homologue.symbol`)
  => :Gene
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  [model path]
  (let [l (last (take-while #(does-not-contain? % :type) (walk model path)))]
    (keyword (or (:referencedType l) (keyword (:name l))))))

(defn relationships
  "Returns all relationships (references and collections) for a given string path.
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  [model path]
  (apply merge (map (get-in model [:classes (class model path)]) [:references :collections])))

(defn mapify [coll]
  (into {} coll))

(defn un-camel-case [s]
  (some-> s
          (clojure.string/replace #"([a-z])([A-Z])" "$1 $2")
          (clojure.string/replace #"([A-Z])([a-z])" " $1$2")
          (clojure.string/replace #"\ +" " ")
          (clojure.string/replace #"^." #(clojure.string/upper-case %))))

(defn display-name
  "Returns a vector of friendly names representing the path.
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  [model path]
  (mapv (fn [prop]
          (or (:displayName prop)
              (un-camel-case (:name prop))))
        (walk model path :walk-properties? true)))

(defn attributes
  "Returns all attributes for a given string path.
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  [model path]
  (apply merge (map (get-in model [:classes (class model path)]) [:attributes])))

(defn class?
  "Returns true if path is a class.
  (class im-model `Gene.diseases`)
  => true
  (class im-model `Gene.diseases.name`)
  => false
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  [model path]
  (let [walked (walk model path)]
    (not (contains? (last walked) :type))))

(defn trim-to-last-class
  "Returns a path string trimmed to the last class
  (trim-to-last-class im-model `Gene.homologues.homologue.symbol`)
  => Gene.homologues.homologue
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  [model path]
  (let [done (take-while #(does-not-contain? % :type) (walk model path))]
    (join-path (take (count done) (split-path path)))))

(defn adjust-path-to-last-class
  "Returns a path adjusted to its last class
  (adjust-path-to-last-class im-model `Gene.organism.name`)
  => Organism.name
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  [model path]
  (let [attribute? (not (class? model path))
        walked     (reverse (walk model path))]
    (if attribute?
      (str (:name (nth walked 1)) "." (:name (nth walked 0)))
      (str (:name (nth walked 0))))))

(defn friendly
  "Returns a path as a strong
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  ([model path & [exclude-root?]]
   (reduce
    (fn [total next]
      (str total (if total " > ") (or (:displayName next) (:name next))))
    nil
    (if exclude-root?
      (rest (walk model path))
      (walk model path)))))

(defn one-of? [col value]
  (some? (some #{value} col)))

(defn subclasses
  "Returns direct subclasses of the class.
  Tip: To get descendant subclasses, you will need to create a graph out of all
  the classes' extends key, which is costly and outside the scope of imcljs.
  Make sure to add :type-constraints to the model if the path traverses a subclass
  (see docstring of `walk` for more information)."
  [model path]
  (let [path-class (class model path)]
    (->> model
         :classes
         (filter (fn [[_ properties]] (one-of? (:extends properties) (name path-class))))
         (map first)
         not-empty)))

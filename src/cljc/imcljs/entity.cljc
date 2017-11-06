(ns imcljs.entity
  (:require [imcljs.internal.utils :refer [one-of?]]))

(defn extended-by
  "Returns classes that directly extend the provided class in a flat structure"
  [model class]
  (reduce (fn [total [class-name class-map]]
            (if (one-of? (:extends class-map) (name class))
              (let [direct-descendants (extended-by model class-name)]
                (merge (assoc total class-name class-map) direct-descendants))
              total)) {} (:classes model)))

(defn ext-by [model class]
  (->> model
       :classes
       (filter #(->> % second :extends (filter #{(name class)}) not-empty))
       (map first)))

(defn extended-by-tree
  "Returns classes that directly extend the provided class in a tree structure"
  [model class]
  (reduce (fn [total [class-name class-map]]
            (if (one-of? (:extends class-map) (name class))
              (let [direct-descendants (extended-by-tree model class-name)]
                (assoc total class-name (assoc class-map :descendants direct-descendants)))
              total)) {} (:classes model)))








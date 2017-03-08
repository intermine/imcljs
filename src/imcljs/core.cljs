(ns imcljs.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [imcljs.fetch :as fetch]
            [cljs.core.async :refer [<!]]
            [cljs-http.client :as client]
            [imcljs.path :as path]
            [imcljs.query :as query]
            [imcljs.save :as save]
            [imcljs.entity :as entity]))

(enable-console-print!)

(def service {:root  "beta.flymine.org/beta"
              :model {:name "genomic"}})

(def query {:from "Gene" :select ["Gene.organism.name"]})

(defn on-js-reload []
  (go
    (let [model (<! (fetch/model service))]
      (println "adjusted" (path/adjust-path-to-last-class model "Gene.organism.name")))))





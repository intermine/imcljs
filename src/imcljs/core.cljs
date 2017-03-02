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
      #_(.log js/console "done"
            (<! (fetch/unique-values service query 5))))))





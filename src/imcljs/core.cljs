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

(defn on-js-reload []


  (go (let [model (<! (fetch/model service))]
        (let [rs (path/subclasses model "Gene.proteins")]
          (.log js/console "sss" rs))))

  )





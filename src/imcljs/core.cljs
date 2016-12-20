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

(defn on-js-reload []
  (.log js/console "Reloading."))
(ns imcljs.saved-queries-test
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.test :refer-macros [async deftest testing is]]
            [cljs.core.async :refer [<!]]
            [imcljs.fetch :as fetch]
            [imcljs.save :as save]
            [imcljs.env :refer [service]]
            [imcljs.auth :as auth]))

(def query
  {:title "my query",
   :from "Gene",
   :select ["Gene.primaryIdentifier"
            "Gene.secondaryIdentifier"
            "Gene.symbol"
            "Gene.name"
            "Gene.length"
            "Gene.organism.shortName"],
   :constraintLogic nil,
   :where []})

(deftest upload-query
  (testing "Should be able to upload a query for saving."
    (async done
      (go
        (let [login-response   (<! (auth/login service "test_user@mail_account" "secret"))
              token            (get-in login-response [:output :token])
              service+auth     (assoc service :token token)
              _upload-query    (<! (save/query service+auth query))
              queries          (<! (fetch/saved-queries service+auth))]
          (is (true? (contains? queries (-> query :title keyword))))
          (done))))))

(deftest delete-query
  (testing "Should be able to delete a saved query."
    (async done
      (go
        (let [login-response   (<! (auth/login service "test_user@mail_account" "secret"))
              token            (get-in login-response [:output :token])
              service+auth     (assoc service :token token)
              _delete_query    (<! (save/delete-query service+auth (:title query)))
              queries          (<! (fetch/saved-queries service+auth))]
          (is (false? (contains? queries (-> query :title keyword))))
          (done))))))

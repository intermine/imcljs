(ns imcljs.utils-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [imcljs.env :refer [service]]
            [imcljs.internal.io :refer [restful]]))

(def args [:get "/lists" service {:name "banana"} (comp first :lists)])

(deftest assert-args
  (testing "throws on empty path string"
    (is (thrown-with-msg? js/Error #"path" (apply restful (assoc args 1 "")))))
  (testing "throws on missing root URL"
    (is (thrown-with-msg? js/Error #"root" (apply restful (update args 2 assoc :root nil)))))
  (testing "throws when options is unexpected type"
    (is (thrown-with-msg? js/Error #"options" (apply restful (update args 3 seq)))))
  (testing "throws when xform is unexpected type"
    (is (thrown-with-msg? js/Error #"xform" (apply restful (assoc args 4 0))))))

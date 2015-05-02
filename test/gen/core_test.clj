(ns gen.core_test
  (:require
    [gen.core :refer [parse-doc]]
    [clojure.test :refer [deftest is]]))

(def example1
"
ignore
this
text

===== Name
cljs.core/foo

===== EmptySection1
===== EmptySection2
===== Description

Hello
World

===== EmptySection3


===== Example#1
N/A

===== Example#2
N/A

===== Example#3
N/A

===== EmptySection4
")

(def example1-parsed
  {"name" "cljs.core/foo"
   "description" "Hello\nWorld"
   :example-ids ["example#1" "example#2" "example#3"]
   "example#1" "N/A"
   "example#2" "N/A"
   "example#3" "N/A"})

(deftest example1-test
  (is (= (parse-doc example1) example1-parsed)))

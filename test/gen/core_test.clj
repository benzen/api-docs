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

===== Empty1
===== Empty2
===== Description

Hello
World

===== Empty3


===== Example#1
N/A

===== Example#2
N/A

===== Example#3
N/A

===== Empty4
")

(def example1-parsed
  {"name" "cljs.core/foo"
   "description" "Hello\nWorld"
   "example#1" "N/A"
   "example#2" "N/A"
   "example#3" "N/A"
   :example-ids ["example#1" "example#2" "example#3"]
   :empty-sections #{"empty1"
                     "empty2"
                     "empty3"
                     "empty4"}
   })

(deftest example1-test
  (is (= (parse-doc example1) example1-parsed)))

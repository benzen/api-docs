# CLJS API Docs

These are manually created docs that will be merged over the [generated docs]
to create all the information for a symbol's doc page.

For example, `assoc-in` has the following format:

    ===== Name
    cljs.core/assoc-in

    ===== Signature
    [m [k & ks] v]

    ===== Description

    Associates a value in a nested associative structure, where `ks` is a sequence
    of keys and `v` is the new value. Returns a new nested structure.

    If any levels do not exist, hash-maps will be created.

    ===== Related
    cljs.core/assoc
    cljs.core/update-in
    cljs.core/dissoc-in
    cljs.core/get-in

    ===== Example#e76f20

    ```
    (def users [{:name "James" :age 26}
                {:name "John" :age 43}])
    ```

    Update the age of the second (index 1) user:

    ```
    (assoc-in users [1 :age] 44)
    ;;=> [{:name "James", :age 26}
    ;;    {:name "John", :age 44}]
    ```

    Insert the password of the second (index 1) user:

    ```
    (assoc-in users [1 :password] "nhoJ")
    ;;=> [{:name "James", :age 26}
    ;;    {:password "nhoJ", :name "John", :age 43}]
    ```

[generated docs]:http://github.com/cljsinfo/api-docs-generated

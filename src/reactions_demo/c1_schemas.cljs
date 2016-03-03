(ns ^:figwheel-always reactions-demo.c1-schemas
  (:require [cljs.test :refer-macros [is testing]]
            [clojure.string :as str]
            [devcards.core :as dc :refer-macros [defcard defcard-doc mkdn-pprint-source deftest mkdn-pprint-code]]
            [reagent.core :as rc]
            [schema.coerce :as coerce]
            [schema.core :as schema]
            [schema.utils :as sutils]))

(def Email (schema/both
            schema/Str
            (schema/pred (partial re-matches #"[^@]+@[-a-z_0-9]+\.[-a-z_0-9]+"))))

(def EmailCI (schema/both
              schema/Str
              (schema/pred (partial re-matches #"(?i)[^@]+@[-a-z_0-9]+\.[-a-z_0-9]+"))))
(def Login
  {:email Email
   :password schema/Str})

(defn normalised-after-schema-check
  [data]
  (if-let [error (schema/check
                  (merge Login
                         ;; Just using a case-insensitive regex for
                         ;; the email. We can merge schema maps like
                         ;; regular maps to make new schemas.
                         {:email EmailCI}) data)]
    ;; We need to make a special case now for returning the error data.
    (sutils/ErrorContainer. error)

    ;; We update the incoming data that we know is validated
    ;; correctly.
    (update-in data [:email] str/lower-case)))

(defcard-doc
  "## Schemas & Coercers

   Schemas are great for making sure that your data looks as it should.

   Coercers are great for forcing your data into the correct form.

   Imagine that we have an email schema:
   "
  (mkdn-pprint-source Email)
  (mkdn-pprint-source Login)

  "This (very naïve) Email schema won't deal with lower-case characters.  We have two options.

1. Maybe we could just add `(?i)` to the start of our regex.
2. Use a coercer.

Option #1 is okay, but in our code we'd need to worry about all places
where we do string matching. It's even worse if it is used as a key to some databases.

If we don't use a coercer, as soon as we get the data, we should normalise it."
  (mkdn-pprint-source normalised-after-schema-check)
  (mkdn-pprint-source EmailCI))

(deftest regular-schema-check
  "## Just testing schemas

   When just testing against a Schema, we know that `nil` is returned
  when we run `(schema/check ...)` otherwise the error map is
  returned."

  (testing "Schema matches"
    (is (nil? (schema/check Login {:email    "david.h@juxt.pro"
                                   :password "pleaseletmein!"}))
        "it should be nil as it passes the schema")
    (is (not (nil? (schema/check Login {:email    "@@@@"
                                        :password "pleaseletmein!"}))))
    (is (=
         "lower-case@juxt.pro"
         (:email (normalised-after-schema-check {:email    "LOWER-CASE@juxt.PRO"
                                                 :password "abc"}))))

    (is (sutils/error? (normalised-after-schema-check {})))))

(def LoginCoercer
  (coerce/coercer
   Login
   ;; type -> coercion function
   {Email (coerce/safe str/lower-case)}))

(defcard-doc
  "## Making a Coercer:"

  "A coercer requires two arguments:"
  "1. The Schema to check against
2. A map of schema types and coercions to perform"
  (mkdn-pprint-source LoginCoercer)

  "The `coercer` function returns a _function_. Pass the input data to this function.

When using a coercer, you can't just check `nil?`, you must check if it is an error-type."
  (mkdn-pprint-code '(let [ret (LoginCoercer {:email "input data"})]
                       (if (sutils/error? ret)
                         (println "Error state.")
                         (println "Good, coerced, data")))
   ))

(deftest coerced-schema-check
  "## Testing with a coercion"

  (testing "A Schema match returns the required data:"
    (is (= {:email    "malcolm@juxt.pro"
            :password "bidi&yada4eva"}
           (LoginCoercer {:email    "MalCoLm@JUXT.pRo"
                          :password "bidi&yada4eva"}))))

  (testing "A schema failure will not return `nil`"
    (is (= sutils/ErrorContainer
           (type (LoginCoercer {}))))

    (testing "but you can easily check for an error:"
      (is (sutils/error? (LoginCoercer {:terrible "data"}))))))

(def CSVNumbers
  [schema/Int])

(def BigSchema
  {:num     schema/Int
   :field1  schema/Str
   :field2  schema/Str
   :numbers CSVNumbers})

(def BigSchemaCoercer
  (coerce/coercer BigSchema
                  {schema/Int (coerce/safe (fn [x]
                                             (some-> x
                                                     str/trim
                                                     (str/replace #"[^0-9]" "")
                                                     (js/parseInt))))
                   schema/Str (coerce/safe (fn [x]
                                             (some-> x
                                                     str/trim
                                                     str/lower-case)))
                   ;; Doing some crazy datastructure building...
                   CSVNumbers (coerce/safe (fn [x]
                                             (as-> x x
                                               ;; split on comma
                                               (str/split x #",")
                                               ;; remove non-digits
                                               (map (fn [t]
                                                      (str/replace t #"[^0-9]" "")) x)
                                               ;; get rid of empty strings
                                               (remove empty? x)
                                               ;; make a vector of ints
                                               (mapv js/parseInt x))))}))

(def coerced (BigSchemaCoercer {:num     "   54"
                                :field1  "  BIG & small TeXt "
                                :field2  " something "
                                :numbers " 0,  5,,,d,10,25    ,bad,9"}))


(defcard-doc
  "## Dealing with complex types:

There is no reason to stick with simple strings, you can coerce to
anything that you wish."
  (mkdn-pprint-source BigSchema)
  (mkdn-pprint-source CSVNumbers)
  (mkdn-pprint-source BigSchemaCoercer)

  "Some data that has been coerced"
  (mkdn-pprint-source coerced))


(deftest coerced-continued
  "## If you can coerce it...


By using coercers, it is possible to validate the input data _but
also_ get your data into a good state.

Below, we have built `:numbers` in to a vector as we validate the input."
  (testing "A bigger example:"
    (testing "we made a number from the string"
      (is (= 54
             (:num coerced))))
    (testing "we cleaned the string"
      (is (= "big & small text"
             (:field1 coerced))))

    (testing "we made a vector from the string"
      (is (= [0 5 10 25 9]
             (:numbers coerced))))))


(defcard-doc
  "## Coercion is amazing :)

Coercing data as part of input validation means that the data is in a
clean, usable form.

You don't sacrifice any schema checking to change
the form. The only change is to the error logic.")

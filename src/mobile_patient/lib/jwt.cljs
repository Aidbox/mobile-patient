(ns mobile-patient.lib.jwt
  (:require [goog.crypt.base64]
            [goog.json]))

(defn get-data-from-token [token]
  (let [[header payload signature] (clojure.string/split token #"\.")]
    (-> payload
        goog.crypt.base64/decodeString
        goog.json.parse
        (js->clj :keywordize-keys true))))

(ns mobile-patient.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [mobile-patient.app :refer [app]]
            [mobile-patient.events]
            [mobile-patient.subs]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))


(defn app-root []
  [(r/adapt-react-class app)])

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent app-registry "MobilePatient" #(r/reactify-component app-root)))

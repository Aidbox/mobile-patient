(ns mobile-patient.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [mobile-patient.routes :refer [routes]]
            [mobile-patient.events]
            [mobile-patient.subs]))

(enable-console-print!)

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))


(defn app-root []
  [(r/adapt-react-class routes)])

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent app-registry "MobilePatient" #(r/reactify-component app-root)))

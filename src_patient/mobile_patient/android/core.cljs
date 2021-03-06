(ns mobile-patient.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [patient.routes :refer [drawer-routes]]
            [mobile-patient.screen.login :refer [LoginScreen]]
            [mobile-patient.screen.demographics :refer [DemographicsScreen]]
            [mobile-patient.events]
            [mobile-patient.subs]
            [patient.events]
            [patient.subs]))

(when-not js/goog.DEBUG (enable-console-print!))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))

(defn app-root []
  (fn []
    (let [screen (subscribe [:get-current-screen])]
      (case @screen
         :login [LoginScreen]
         :demographics [DemographicsScreen]
         :main [(r/adapt-react-class drawer-routes)]))))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "MobilePatient" #(r/reactify-component app-root)))

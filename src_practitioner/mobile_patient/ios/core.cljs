(ns mobile-patient.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [practitioner.routes :refer [drawer-routes]]
            [mobile-patient.ui :as ui]
            [practitioner.screen.login :refer [LoginScreen]]
            [mobile-patient.events]
            [mobile-patient.subs]
            [practitioner.events]
            [practitioner.subs]))

(when-not js/goog.DEBUG (enable-console-print!))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))


(defn app-root []
  (fn []
    (let [screen (subscribe [:get-current-screen])]
      (case @screen
         :login [LoginScreen]
         :main [(r/adapt-react-class drawer-routes)]))))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "MobilePatient" #(r/reactify-component app-root)))

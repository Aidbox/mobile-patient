(ns mobile-patient.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [practitioner.routes :refer [drawer-routes on-navigation-callback]]
            [practitioner.events]
            [mobile-patient.events]
            [mobile-patient.subs]
            [mobile-patient.screen.login :refer [LoginScreen]]
            [mobile-patient.ui :as ui]
            ))

(when-not js/goog.DEBUG (enable-console-print!))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))


(defn app-root []
  (fn []
    (let [screen (subscribe [:get-current-screen])]
      (case @screen
         :login [LoginScreen]
         :main [(r/adapt-react-class drawer-routes) {:onNavigationStateChange on-navigation-callback}]))))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "MobilePatient" #(r/reactify-component app-root)))

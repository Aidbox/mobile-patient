(ns mobile-patient.nav)


(def react-navigation (js/require "react-navigation"))
(def NavigationActions (.-NavigationActions react-navigation))


(def demographics
  (.reset NavigationActions
          (clj->js {:index 0
                    :actions [(.navigate NavigationActions #js {:routeName "Demographics"})]})))


(def tabs
  (.reset NavigationActions
          (clj->js {:index 0
                    :actions [(.navigate NavigationActions #js {:routeName "Tabs"})]})))

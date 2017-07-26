(ns mobile-patient.routes
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.route-helpers :as rh]
            [mobile-patient.screen.patients :refer [PatientsScreen]]
            [mobile-patient.screen.vitals :refer [VitalsScreen]]
            [mobile-patient.screen.meds :refer [MedsScreen]]
            [mobile-patient.screen.chat :refer [ChatsScreen ChatScreen]]
            [mobile-patient.screen.settings :refer [SettingsScreen]]
            [mobile-patient.screen.contacts :refer [ContactsScreen]]
            [mobile-patient.screen.chart :refer [ChartScreen]]))

(defn dumb-component [text]
  (r/reactify-component (fn [] [ui/text text])))

(def routes
  (clj->js
   {"Medications"
    {:screen (rh/stack-navigator
              {"Meds" {:screen (r/reactify-component MedsScreen)
                       :navigationOptions (rh/drawer-nav-opts "Medications")}})}



    "Vitals Signs"
    {:screen (rh/stack-navigator
              {"Vitals" {:screen (r/reactify-component VitalsScreen)
                         :navigationOptions (rh/drawer-nav-opts "Vitals Signs")}

               "ExampleChart" {:screen (r/reactify-component ChartScreen)
                               :navigationOptions (rh/drawer-nav-opts "Example Chart" nil :back)
                               }})}

    "Chats"
    {:screen (rh/stack-navigator
              {"Chats" {:screen (r/reactify-component ChatsScreen)
                        :navigationOptions (rh/drawer-nav-opts "Chats" rh/add-chat-button)}

               "Chat" {:screen (r/reactify-component ChatScreen)
                       :navigationOptions (fn [props]
                                            (let [chat-name (-> props .-navigation .-state .-params (aget "chat-name"))]
                                              ((rh/drawer-nav-opts chat-name nil :back) props)))}

               "Contacts" {:screen (r/reactify-component ContactsScreen)
                           :navigationOptions (rh/drawer-nav-opts "Add Person" nil :back)}})}



    "Settings"
    {:screen (rh/stack-navigator
              {"Settings" {:screen (r/reactify-component SettingsScreen)
                           :navigationOptions (rh/drawer-nav-opts "Settings")}})}



    "About App"
    {:screen (rh/stack-navigator
              {"About App" {:screen (dumb-component "About App")
                            :navigationOptions (rh/drawer-nav-opts "About App")}})}


    "Patients"
    {:screen (rh/stack-navigator
              {"Patients" {:screen (r/reactify-component PatientsScreen)
                           :navigationOptions (rh/drawer-nav-opts "Patients")}})}
    }))

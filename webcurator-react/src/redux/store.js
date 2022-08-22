import { combineReducers, configureStore } from "@reduxjs/toolkit";
import targetsReducer from './targets';

const rootReducer = combineReducers({
    targets: targetsReducer,
})

export const setupStore = preloadedState => {
    return configureStore({
        reducer: rootReducer,
        preloadedState
    })
}
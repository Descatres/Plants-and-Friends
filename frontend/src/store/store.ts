import { configureStore } from "@reduxjs/toolkit";
import { persistStore, persistReducer } from "redux-persist";
import storage from "redux-persist/lib/storage";
import tokenReducer from "./slices/tokenSlice";
import pineTreeReducer from "./slices/pineTreeSlice";

const persistConfigToken = {
  key: "token",
  storage,
};

const persistedTokenReducer = persistReducer(persistConfigToken, tokenReducer);

const store = configureStore({
  reducer: {
    token: persistedTokenReducer,
    pineTree: pineTreeReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({ serializableCheck: false }),
  devTools: process.env.NODE_ENV !== "production", // TODO check if this is working
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export const persistor = persistStore(store);
export default store;

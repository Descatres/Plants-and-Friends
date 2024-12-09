import { createSlice } from "@reduxjs/toolkit";
import * as THREE from "three";

export const pineTreeSlice = createSlice({
  name: "pineTree",
  initialState: {
    model: null as THREE.Object3D | null,
  },
  reducers: {
    setModel: (state, action: { payload: THREE.Object3D; type: string }) => {
      state.model = action.payload;
    },
    clearModel: (state) => {
      state.model = null;
    },
  },
});

export const { setModel, clearModel } = pineTreeSlice.actions;

export default pineTreeSlice.reducer;

import { createSlice } from "@reduxjs/toolkit";

export const tokenSlice = createSlice({
  name: "token",
  initialState: {
    value: null as string | null,
  },
  reducers: {
    setToken: (state, action: { payload: string; type: string }) => {
      state.value = action.payload;
    },
    removeToken: (state, action) => {
      state.value = null;
    },
  },
});

export const { setToken, removeToken } = tokenSlice.actions;

export default tokenSlice.reducer;

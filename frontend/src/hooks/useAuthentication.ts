import {
  LOGIN_URL,
  REGISTER_URL,
} from "../utils/routesAndEndpoints/routesAndEndpoints";
import { useApi } from "../hooks/useApi";
import { useDispatch } from "react-redux";
import { removeToken, setToken } from "../store/slices/tokenSlice";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

export function useAuthentication() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { api } = useApi();
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const login = async (data: any) => {
    setIsLoading(true);

    api
      .post(LOGIN_URL, {
        email: data.email,
        password: data.password,
      })
      .then((response: any) => {
        const { token } = response.data;

        if (token) {
          console.log("token", token);
          toast.success("Welcome!");
          dispatch(setToken(token));
          navigate("/home");
        }
      })
      .catch((error: any) => {
        const errors = error.response?.data?.errors || [
          "Email or password is incorrect",
        ];

        console.log("error", errors);
        toast.error("Email or password is incorrect");
        navigate("/login");
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  const register = async (data: any) => {
    setIsLoading(true);

    api
      .post(REGISTER_URL, {
        email: data.email,
        password: data.password,
      })
      .then(() => {
        toast.success("Account created successfully");
        navigate("/login");
      })
      .catch((error: any) => {
        const errors = error.response?.data?.errors || [
          "Failed to create account",
        ];

        console.log("error", errors);
        toast.error("Failed to create account");
      })
      .finally(() => {
        setIsLoading(false);
      });
  };

  const logout = () => {
    dispatch(removeToken(null));
    navigate("/");
  };

  return {
    login,
    logout,
    isLoading,
  };
}

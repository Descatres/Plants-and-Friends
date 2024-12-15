import { LOGIN_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { useApi } from "../hooks/useApi";
import { useDispatch } from "react-redux";
import { removeToken, setToken } from "../store/slices/tokenSlice";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

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
          dispatch(setToken(token));
          navigate("/home");
          // const decodedToken: any = jwtDecode(token);
        }
      })
      .catch((error: any) => {
        const errors = error.response?.data?.errors || [
          "O email ou senha estão incorrectos",
        ];
        console.log("error", errors);
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

import jwtDecode from "jwt-decode";
import { LOGIN_URL } from "../../utils/endpoints";
import { useApi } from "../api/useApi";
import { useDispatch } from "react-redux";
import { removeToken, setToken } from "../store/slices/tokenSlice";
import { removeUser, setUser } from "../slices/userSlice";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { User } from "../types/User";

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

          const decodedToken: any = jwtDecode(token);

          const user: User = {
            id: decodedToken.id,
            name: decodedToken.name,
            email: decodedToken.email,
            // isLogged: boolean;
          };

          dispatch(setUser(user));
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
    dispatch(removeUser(null));
    navigate("/");
  };

  return {
    login,
    logout,
    isLoading,
  };
}

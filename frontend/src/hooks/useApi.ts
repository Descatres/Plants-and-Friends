import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../store/store";
import axios from "axios";
import { jwtDecode } from "jwt-decode";
import { removeToken, setToken } from "../store/slices/tokenSlice";
import { useNavigate } from "react-router-dom";
import {
  REFRESH_TOKEN,
  FORBIDDEN_ROUTE,
  INTERNAL_ERROR_ROUTE,
  LANDING_PAGE_ROUTE,
} from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

const API_URL = import.meta.env.VITE_API_URL;

export var isRefreshing = false;

const REFRESH_WINDOW_MS = 300000; // 5 minutes

type DecodedToken = {
  nameid: string;
};

export function useApi() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const { value: token } = useSelector((state: RootState) => state.token);

  const api = axios.create({
    baseURL: API_URL,
  });

  api.interceptors.request.use(
    async (config) => {
      const controller = new AbortController();

      if (token) {
        let newToken = null;

        const decodedToken = jwtDecode<DecodedToken>(token);
        const userId = decodedToken.nameid;

        if (
          isTokenExpiringSoon(decodedToken) &&
          config.url !== REFRESH_TOKEN &&
          !isRefreshing
        ) {
          try {
            isRefreshing = true;
            const response = await refreshToken(userId, token);

            newToken = response?.data?.token;

            if (!newToken) throw new Error();
            else dispatch(setToken(newToken));
          } catch (error: any) {
            toast.error("A sua sessão expirou");
            console.error(error);
            dispatch(removeToken(null));
            controller.abort();
          } finally {
            isRefreshing = false;
          }
        }

        config.headers["Authorization"] = `Bearer ${newToken ?? token}`;
      }

      return {
        ...config,
        signal: controller.signal,
      };
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  api.interceptors.response.use(
    (response) => response,
    (error) => {
      const status = error.response ? error.response.status : null;
      if (status === 401) {
        dispatch(removeToken(null));
        navigate(LANDING_PAGE_ROUTE);
      } else if (status === 403) {
        navigate(FORBIDDEN_ROUTE);
      } else if ([500, 502, 503].includes(status)) {
        navigate(INTERNAL_ERROR_ROUTE);
      } else return Promise.reject(error);
    }
  );

  const isTokenExpiringSoon = (decodedToken: any) => {
    const exp: number = decodedToken.exp;
    const currMillis = Date.now();

    return currMillis >= exp * 1000 - REFRESH_WINDOW_MS;
  };

  const refreshToken = (userId: any, token: string) => {
    return api.post(REFRESH_TOKEN, { userId: userId, userToken: token });
  };

  return { api };
}

import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import {
  HUMIDITY_URL,
  TEMPERATURE_URL,
} from "../utils/routesAndEndpoints/routesAndEndpoints";
// import { toast } from "react-toastify";

export function useFetchRoomStats() {
  const { api } = useApi();

  const [temperature, setTemperature] = useState<number | null>(null);
  const [humidity, setHumidity] = useState<number | null>(null);

  const getTemperature = useCallback(() => {
    api
      .get(TEMPERATURE_URL)
      .then((response: any) => {
        setTemperature(response.data.temperature);
      })
      .catch((error: any) => {
        // toast.error("An error as occurred getting the temperature");
        console.log(error);
        if (error.code) return;
      });
  }, [api]);

  const getHumidity = useCallback(() => {
    api
      .get(HUMIDITY_URL)
      .then((response: any) => {
        setHumidity(response.data.humidity);
      })
      .catch((error: any) => {
        // toast.error("An error as occurred getting the humidity");
        console.log(error);
        if (error.code) return;
      });
  }, [api]);

  return {
    temperature,
    humidity,
    getTemperature,
    getHumidity,
  };
}

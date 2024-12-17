import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import {
  HUMIDITY_URL,
  TEMPERATURE_URL,
} from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

export function useFetchPlants() {
  const { api } = useApi();

  const [isLoadingTemperature, setIsLoadingTemperature] =
    useState<boolean>(false);
  const [isLoadingHuminity, setIsLoadingHuminity] = useState<boolean>(false);
  const [temperature, setTemperature] = useState<number | null>(null);
  const [humidity, setHumidity] = useState<number | null>(null);
  const [errorFindingData, setErrorFindingData] = useState(false);

  const getTemperature = useCallback(() => {
    setIsLoadingTemperature(true);
    setErrorFindingData(false);
    api
      .get(TEMPERATURE_URL)
      .then((response: any) => {
        setTemperature(response.data.temperature);
      })
      .catch((error: any) => {
        toast.error("An error as occurred getting the temperature");
        setErrorFindingData(true);
        console.log(error);
        if (error.code) return;
      })
      .finally(() => {
        setIsLoadingTemperature(false);
      });
  }, [api]);

  const getHumidity = useCallback(() => {
    setIsLoadingHuminity(true);
    setErrorFindingData(false);
    api
      .get(HUMIDITY_URL)
      .then((response: any) => {
        setHumidity(response.data.humidity);
      })
      .catch((error: any) => {
        toast.error("An error as occurred getting the humidity");
        setErrorFindingData(true);
        console.log(error);
        if (error.code) return;
      })
      .finally(() => {
        setIsLoadingHuminity(false);
      });
  }, [api]);

  return {
    isLoadingTemperature,
    isLoadingHuminity,
    temperature,
    humidity,
    errorFindingData,
    getTemperature,
    getHumidity,
  };
}

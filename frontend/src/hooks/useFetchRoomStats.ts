import { useCallback, useState } from "react";
import { SENSOR_DATA_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { useApi } from "./useApi";

export function useFetchRoomStats() {
  const { api } = useApi();
  const [temperature, setTemperature] = useState<number | undefined>(undefined);
  const [humidity, setHumidity] = useState<number | undefined>(undefined);

  const getRoomSensorData = useCallback(() => {
    api
      .get(SENSOR_DATA_URL)
      .then((response: any) => {
        setTemperature(response.data.temperature);
        setHumidity(response.data.humidity);
      })
      .catch((error: any) => {
        console.error("Sensors disconnected");
        setTemperature(undefined);
        setHumidity(undefined);
        return;
      });
  }, [api]);

  return {
    temperature,
    humidity,
    getRoomSensorData,
  };
}

import { useCallback, useState } from "react";
import { SENSOR_DATA_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { useApi } from "./useApi";

export function useFetchRoomStats() {
  const { api } = useApi();
  const [temperature, setTemperature] = useState<number | null>(null);
  const [humidity, setHumidity] = useState<number | null>(null);

  const getRoomSensorData = useCallback(() => {
    const eventSource = new EventSource(
      `${api.defaults.baseURL}${SENSOR_DATA_URL}`
    );

    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);
      setTemperature(data.temperature);
      setHumidity(data.humidity);
    };

    eventSource.onerror = (error) => {
      console.error("Error with sensor:", error);
      eventSource.close();
    };
  }, []);

  return {
    temperature,
    humidity,
    getRoomSensorData,
  };
}

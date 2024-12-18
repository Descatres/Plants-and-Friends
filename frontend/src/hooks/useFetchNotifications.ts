import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import { NOTIFICATIONS_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

export function useFetchNotifications() {
  const { api } = useApi();

  const [notifications, setNotifications] = useState<any[]>([]);
  const [errorFindingData, setErrorFindingData] = useState(false);

  const getNotifications = useCallback(() => {
    setErrorFindingData(false);
    api
      .get(NOTIFICATIONS_URL)
      .then((response: any) => {
        setNotifications(response.data);
      })
      .catch((error: any) => {
        toast.error("An error has occurred getting the plants!");
        setErrorFindingData(true);
        console.log(error);
        if (error.code) return;
      });
  }, [api]);

  return {
    notifications,
    setNotifications,
    errorFindingData,
    getNotifications,
  };
}

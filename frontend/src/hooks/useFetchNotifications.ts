import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import { NOTIFICATIONS_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

export function useFetchNotifications() {
  const { api } = useApi();

  const [notifications, setNotifications] = useState<any[]>([]);

  const getNotifications = useCallback(() => {
    api
      .get(NOTIFICATIONS_URL)
      .then((response: any) => {
        if (Array.isArray(response.data.notifications)) {
          setNotifications(response.data.notifications);
        } else {
          console.error(
            "Expected an array but got:",
            response.data.notifications
          );
          setNotifications([]);
        }
      })
      .catch((error: any) => {
        return;
      });
  }, [api]);

  return {
    notifications,
    setNotifications,
    getNotifications,
  };
}

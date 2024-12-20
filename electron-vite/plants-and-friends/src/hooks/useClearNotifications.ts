import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import { DELETE_NOTIFICATION_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

export function useClearNotifications() {
  const { api } = useApi();

  const clearNotifications = useCallback(() => {
    api
      .delete(DELETE_NOTIFICATION_URL)
      .then(() => {
        toast.success("Notifications removed successfully!");
      })
      .catch((error: any) => {
        toast.error("An error has occurred removing the notifications!");
        console.log(error);
        if (error.code) return;
      });
  }, [api]);

  return {
    clearNotifications,
  };
}

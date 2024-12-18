import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import { Plant } from "../types/Plant";
import { PLANT_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

export function useDeletePlant() {
  const { api } = useApi();

  const [isDeletingPlant, setIsDeletingPlant] = useState<boolean>(false);
  const [errorFindingData, setErrorFindingData] = useState(false);

  const deletePlant = useCallback(
    (id: any) => {
      setIsDeletingPlant(true);
      setErrorFindingData(false);
      // if (id) {
      //   toast.error("An error has occurred getting the plant data!");
      //   setErrorFindingData(true);
      //   setIsDeletingPlant(false);
      //   return;
      // }
      const updateUrl = PLANT_URL.replace(":id", id);

      api
        .delete(updateUrl)
        .then(() => {
          toast.success("Plant deleted successfully!");
        })
        .catch((error: any) => {
          toast.error("An error has occurred deleting the plant!");
          setErrorFindingData(true);
          console.log(error);
          if (error.code) return;
        })
        .finally(() => {
          setIsDeletingPlant(false);
        });
    },
    [api]
  );

  return {
    isDeletingPlant,
    errorFindingData,
    deletePlant,
  };
}

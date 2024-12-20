import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import { Plant } from "../types/Plant";
import { PLANT_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

export function useUpdatePlant() {
  const { api } = useApi();

  const [isUpdatingPlant, setIsUpdatingPlant] = useState<boolean>(false);
  const [errorFindingData, setErrorFindingData] = useState(false);

  const updatePlant = useCallback(
    (plant: Plant) => {
      setIsUpdatingPlant(true);
      setErrorFindingData(false);
      if (!plant._id) {
        toast.error("An error has occurred getting the plant data!");
        setErrorFindingData(true);
        setIsUpdatingPlant(false);
        return;
      }
      const updateUrl = PLANT_URL.replace(":id", plant._id);

      api
        .patch(updateUrl, plant)
        .then(() => {
          toast.success("Plant created successfully!");
        })
        .catch((error: any) => {
          toast.error("An error has occurred getting the plant data!");
          setErrorFindingData(true);
          console.log(error);
          if (error.code) return;
        })
        .finally(() => {
          setIsUpdatingPlant(false);
        });
    },
    [api]
  );

  return {
    isUpdatingPlant,
    errorFindingData,
    updatePlant,
  };
}

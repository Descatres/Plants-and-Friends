import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import { Plant } from "../types/Plant";
import { PLANTS_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

export function useCreatePlant() {
  const { api } = useApi();

  const [isCreatingPlant, setIsCreatingPlant] = useState<boolean>(false);
  const [errorFindingData, setErrorFindingData] = useState(false);

  const createPlant = useCallback(
    (plant: Plant) => {
      setIsCreatingPlant(true);
      setErrorFindingData(false);
      api
        .post(PLANTS_URL, plant)
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
          setIsCreatingPlant(false);
        });
    },
    [api]
  );

  return {
    isCreatingPlant,
    errorFindingData,
    createPlant,
  };
}

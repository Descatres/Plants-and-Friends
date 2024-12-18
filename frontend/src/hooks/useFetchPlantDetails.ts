import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import { Plant } from "../types/Plant";
import { PLANT_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

export function useFetchPlantDetails() {
  const { api } = useApi();

  const [isLoadingPlantData, setIsLoadingPlantData] = useState<boolean>(false);
  const [plantData, setPlantData] = useState<Plant>();
  const [errorFindingData, setErrorFindingData] = useState(false);

  const getPlantData = useCallback(
    (id: string) => {
      setIsLoadingPlantData(true);
      setErrorFindingData(false);
      api
        .get(PLANT_URL.replace(":id", id))
        .then((response: any) => {
          setPlantData(response.data);
        })
        .catch((error: any) => {
          toast.error("An error has occurred getting the plant data!");
          setErrorFindingData(true);
          console.log(error);
          if (error.code) return;
        })
        .finally(() => {
          setIsLoadingPlantData(false);
        });
    },
    [api]
  );

  return {
    isLoadingPlantData,
    plantData,
    errorFindingData,
    getPlantData,
  };
}

import { useCallback, useState } from "react";
import { useApi } from "./useApi";
import { Plant } from "../types/Plant";
import { PLANTS_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";
import { toast } from "react-toastify";

export function useFetchPlants() {
  const { api } = useApi();

  const [isLoadingPlants, setIsLoadingPlants] = useState<boolean>(false);
  const [plants, setPlants] = useState<Plant[]>([]);
  const [totalPlants, setTotalPlants] = useState<number>(0);
  const [errorFindingData, setErrorFindingData] = useState(false);

  const getAllPlants = useCallback(() => {
    setIsLoadingPlants(true);
    setErrorFindingData(false);
    api
      .get(PLANTS_URL)
      .then((response: any) => {
        setPlants(response.data);
        setTotalPlants(response.data.totalItems);
      })
      .catch((error: any) => {
        toast.error("An error has occurred getting the plants!");
        setErrorFindingData(true);
        console.log(error);
        if (error.code) return;
      })
      .finally(() => {
        setIsLoadingPlants(false);
      });
  }, [api]);

  return {
    isLoadingPlants,
    plants,
    totalPlants,
    errorFindingData,
    getAllPlants,
  };
}

import { useCallback, useEffect, useState } from "react";
import { useApi } from "./useApi";
// import { toaster } from "components/ui/toaster";
import { Plant } from "../types/Plant";
import { PLANTS_URL } from "../utils/routesAndEndpoints/routesAndEndpoints";

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
        setPlants(response.data.result);
        setTotalPlants(response.data.totalItems);
      })
      .catch((error: any) => {
        if (error.code === "ERR_CANCELED") return;

        // toaster.create({
        //   type: "error",
        //   title: "Erro",
        //   description: "Ocorreu um erro ao buscar os dados",
        // });
        console.log("API Error:", error);
        setErrorFindingData(true);
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

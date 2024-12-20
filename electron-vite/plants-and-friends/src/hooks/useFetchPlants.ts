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
  const [currentPage, setCurrentPage] = useState<number>(1);

  const getPlants = useCallback(
    (page: number, limit: number) => {
      if (page < 1) return;

      console.log(`Fetching plants: page=${page}, limit=${limit}`);

      setIsLoadingPlants(true);
      setErrorFindingData(false);

      api
        .get(PLANTS_URL, { params: { page, limit } })
        .then((response: any) => {
          if (response.data.plants && response.data.plants.length > 0) {
            console.log(`Received ${response.data.plants.length} plants.`);
            setPlants((prev) => [
              ...prev,
              ...response.data.plants.filter(
                (plant: Plant) => !prev.some((p) => p._id === plant._id)
              ),
            ]);
            setTotalPlants(response.data.totalItems);
          } else {
            console.log("No more plants to load.");
          }
        })
        .catch((error: any) => {
          if (!toast.isActive("fetchPlantsError")) {
            toast.error("An error has occurred getting the plants!", {
              toastId: "fetchPlantsError",
            });
          }
          setErrorFindingData(true);
          console.error(error);
        })
        .finally(() => {
          setIsLoadingPlants(false);
        });
    },
    [api]
  );

  return {
    isLoadingPlants,
    plants,
    totalPlants,
    errorFindingData,
    getPlants,
    currentPage,
    setCurrentPage,
  };
}

import { useEffect, useState } from "react";
import classes from "./Home.module.css";
import PlantCard from "../../Components/PlantCard/PlantCard";
import Spinner from "../../Components/Spinner/Spinner";
import Error from "../../Components/Errors/Errors";
import { useFetchPlants } from "../../hooks/useFetchPlants";

function Home() {
  const { getAllPlants, isLoadingPlants, plants, errorFindingData } =
    useFetchPlants();

  useEffect(() => {
    getAllPlants();
  }, []);

  return (
    <div className={classes.mainContainer}>
      <div className={classes.plantsContainer}>
        {plants &&
          plants.map((plant, index) => (
            <div key={index}>
              <div className={classes.plant} key={plant.id}>
                <PlantCard
                  id={plant.id}
                  name={plant.name}
                  species={plant.species ?? ""}
                  minTemperature={plant.minTemperature ?? undefined}
                  maxTemperature={plant.maxTemperature ?? undefined}
                  minHumidity={plant.minHumidity ?? undefined}
                  maxHumidity={plant.maxHumidity ?? undefined}
                  imageUrl={plant.imageUrl ?? ""}
                  isList
                />
              </div>
            </div>
          ))}
        {errorFindingData && <Error />}
        {isLoadingPlants && <Spinner />}
      </div>
    </div>
  );
}

export default Home;

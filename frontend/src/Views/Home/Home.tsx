import React, { useEffect, useState } from "react";
import classes from "./Home.module.css";
import PlantCard from "../../Components/PlantCard/PlantCard";
import { useSelector } from "react-redux";
import { RootState } from "@react-three/fiber";

function Home() {
  const [plantData, setPlantData] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);
  const token = useSelector((state: RootState) => state.token.value);

  useEffect(() => {
    fetch("http://localhost:5001/", {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error("Failed to fetch plant data");
        }
        console.log();
        return response.json();
      })
      .then((data) => {
        console.log(data);
        setPlantData(data);
      })
      .catch((error) => setError(error.message));
  }, []);

  if (error) {
    return <div>Error: {error}</div>;
  }

  return (
    <div className={classes.mainContainer}>
      <div className={classes.plantsContainer}>
        {plantData.map((plant, index) => (
          <div key={index}>
            <div className={classes.plant} key={plant.id}>
              <PlantCard
                id={plant.id}
                name={plant.name}
                species={plant.species}
                temperature={{
                  min: plant.minTemperature,
                  max: plant.maxTemperature,
                }}
                humidity={{ min: plant.minHumidity, max: plant.maxHumidity }}
                onwerId={plant.onwerId}
                isList
              />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default Home;

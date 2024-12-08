import React, { useEffect, useState } from "react";
import classes from "./Home.module.css";
import PlantCard from "../../Components/PlantCard/PlantCard";

function Home() {
  const token = localStorage.getItem("token");
  const [plantData, setPlantData] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);

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
      .then((data) => setPlantData(data))
      .catch((error) => setError(error.message));
  }, []);

  if (error) {
    return <div>Error: {error}</div>;
  }
  
  return (
    <div className={classes.mainContainer}>
      <div style={{ display: "flex", gap: "2rem", flexWrap: "wrap" }}>
        {plantData.map((plant, index) => (
          <>
            <PlantCard
              key={index}
              id={plant.id}
              name={plant.name}
              species={plant.species}
              temperature={{ min: plant.minTemperature, max: plant.maxTemperature }}
              humidity={{ min: plant.minHumidity, max: plant.maxHumidity }}
              onwerId={plant.onwerId}
              isList
            />
          </>
        ))}
      </div>
    </div>
  );
}

export default Home;

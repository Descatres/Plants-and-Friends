import React from "react";
import classes from "./Home.module.css";
import PlantCard from "../../Components/PlantCard/PlantCard";

function Home() {
  const plantData = [
    {
      id: 1,
      name: "Plant 1",
      species: "Specie 1",
      temperature: { min: 20, max: 25 },
      humidity: { min: 40, max: 60 },
    },
    {
      id: 2,
      name: "Plant 1",
      species: "Specie 1",
      temperature: { min: 20, max: 25 },
      humidity: { min: 40, max: 60 },
    },
  ];
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
              temperature={plant.temperature}
              humidity={plant.humidity}
              isList
            />
            {/* <PlantCard
              key={index}
              id={plant.id}
              name={plant.name}
              species={plant.species}
              temperature={plant.temperature}
              humidity={plant.humidity}
            />
            <PlantCard
              key={index}
              id={plant.id}
              name={plant.name}
              species={plant.species}
              temperature={plant.temperature}
              humidity={plant.humidity}
            /> */}
          </>
        ))}
      </div>
    </div>
  );
}

export default Home;

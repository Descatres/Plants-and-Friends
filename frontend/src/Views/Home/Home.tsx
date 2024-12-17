import { useEffect, useState } from "react";
import classes from "./Home.module.css";
import PlantCard from "../../Components/PlantCard/PlantCard";
import Spinner from "../../Components/Spinner/Spinner";
import Error from "../../Components/Errors/Errors";
import { useFetchPlants } from "../../hooks/useFetchPlants";
import RoomStats from "../../Components/RoomStats/RoomStats";
import ListGridSwapper from "../../Components/ListGridSwapper/ListGridSwapper";
import Search from "../../Components/Search/Search";

function Home() {
  const { getAllPlants, isLoadingPlants, plants, errorFindingData } =
    useFetchPlants();
  const [isList, setIsList] = useState(false);
  const [searchQuery, setSearchQuery] = useState<string>("");

  useEffect(() => {
    getAllPlants();
  }, []);

  const handleIsList = () => {
    setIsList(true);
  };
  const handleIsGrid = () => {
    setIsList(false);
  };

  const handleSearch = (query: string) => {
    setSearchQuery(query.toLowerCase());
  };

  const filteredPlants = plants?.filter(
    (plant) =>
      plant.name.toLowerCase().includes(searchQuery) ||
      plant.species?.toLowerCase().includes(searchQuery)
  );

  return (
    <div className={classes.mainContainer}>
      <div className={classes.content}>
        <div className={classes.tweaksContainer}>
          <div className={classes.listGridSwapperContainer}>
            <ListGridSwapper
              isList={isList}
              handleIsList={handleIsList}
              handleIsGrid={handleIsGrid}
            />
          </div>
          <div className={classes.roomStatsContainer}>
            <RoomStats />
          </div>
          <div className={classes.searchContainer}>
            <Search onSearch={handleSearch} />
          </div>
        </div>
        <div className={classes.plantsContainer}>
          <div className={isList ? classes.plantRowList : classes.plantRowGrid}>
            {filteredPlants && filteredPlants.length > 0 ? (
              filteredPlants.map((plant, index) => (
                <div key={index}>
                  <div key={plant.id} className={classes.plant}>
                    <PlantCard
                      id={plant.id}
                      name={plant.name}
                      species={plant.species ?? ""}
                      minTemperature={plant.minTemperature ?? undefined}
                      maxTemperature={plant.maxTemperature ?? undefined}
                      minHumidity={plant.minHumidity ?? undefined}
                      maxHumidity={plant.maxHumidity ?? undefined}
                      imageUrl={plant.imageUrl ?? ""}
                      isList={isList}
                    />
                  </div>
                </div>
              ))
            ) : (
              <p>No plants match your search.</p>
            )}
            {errorFindingData && <Error />}
            {isLoadingPlants && <Spinner />}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Home;

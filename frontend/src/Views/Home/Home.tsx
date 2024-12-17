import { useEffect, useState } from "react";
import classes from "./Home.module.css";
import PlantCard from "../../Components/PlantCard/PlantCard";
import Spinner from "../../Components/Spinner/Spinner";
import { useFetchPlants } from "../../hooks/useFetchPlants";
import RoomStats from "../../Components/RoomStats/RoomStats";
import ListGridSwapper from "../../Components/ListGridSwapper/ListGridSwapper";
import Search from "../../Components/Search/Search";
import DropdownMenu from "../../Components/DropdownMenu/DropdownMenu";
import ErrorPage from "../../errorPages/ErrorPage";
import { useNavigate } from "react-router-dom";
import { createPortal } from "react-dom";

function Home() {
  const { getAllPlants, isLoadingPlants, plants, errorFindingData } =
    useFetchPlants();
  const [isList, setIsList] = useState(false);
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [sortBy, setSortBy] = useState<string | "">("");
  const [isDescending, setIsDescending] = useState<boolean>(false);
  const navigate = useNavigate();

  useEffect(() => {
    getAllPlants();
  }, []);

  useEffect(() => {
    if (!sortBy) setIsDescending(false);
  }, [sortBy]);

  const handleIsList = () => {
    setIsList(true);
  };
  const handleIsGrid = () => {
    setIsList(false);
  };

  const handleSearch = (query: string) => {
    setSearchQuery(query.toLowerCase());
  };

  const handleSortBy = (field: string) => {
    setSortBy(field);
  };

  const handleSortOrder = () => {
    setIsDescending(!isDescending);
  };
  const clearSort = () => {
    setSortBy("");
  };

  const filteredPlants = plants
    ?.filter(
      (plant) =>
        plant.name.toLowerCase().includes(searchQuery) ||
        plant.species?.toLowerCase().includes(searchQuery)
    )
    .sort((a: any, b: any) => {
      if (!sortBy) return 0;
      const valueA = a[sortBy.toLowerCase()]?.toLowerCase();
      const valueB = b[sortBy.toLowerCase()]?.toLowerCase();
      if (valueA < valueB) return isDescending ? 1 : -1;
      if (valueA > valueB) return isDescending ? -1 : 1;
      return 0;
    });

  return (
    <div className={classes.mainContainer}>
      <div className={classes.content}>
        <div className={classes.controlsContainer}>
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
          <div className={classes.filtersContainer}>
            <DropdownMenu
              button={<div className={classes.filterCardSelect}>Sort By</div>}
              options={[
                {
                  id: 1,
                  name: "Name",
                  onClick: () => handleSortBy("Name"),
                },
                {
                  id: 2,
                  name: "Species",
                  onClick: () => handleSortBy("Species"),
                },
              ]}
            />
            <div
              className={sortBy ? classes.orderCard : classes.orderCardDisabled}
              onClick={sortBy ? handleSortOrder : undefined}
            >
              {isDescending ? "↓" : "↑"}
            </div>
            {sortBy && (
              <div className={classes.filterCard} onClick={clearSort}>
                ✖ {sortBy}
              </div>
            )}
          </div>
        </div>
        <div className={classes.plantsContainer}>
          <div className={isList ? classes.plantRowList : classes.plantRowGrid}>
            {filteredPlants && filteredPlants.length > 0
              ? filteredPlants.map((plant, index) => (
                  <div key={index}>
                    <div key={plant.id} className={classes.plant}>
                      <PlantCard
                        id={plant.name}
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
              : !errorFindingData && <p>No plants match your search.</p>}
            {errorFindingData && createPortal(<ErrorPage />, document.body)}
            {isLoadingPlants && <Spinner />}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Home;

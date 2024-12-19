import { useCallback, useEffect, useRef, useState } from "react";
import classes from "./Home.module.css";
import PlantCard from "../../Components/PlantCard/PlantCard";
import Spinner from "../../Components/Spinner/Spinner";
import { useFetchPlants } from "../../hooks/useFetchPlants";
import RoomStats from "../../Components/RoomStats/RoomStats";
import ListGridSwapper from "../../Components/ListGridSwapper/ListGridSwapper";
import Search from "../../Components/Search/Search";
import DropdownMenu from "../../Components/DropdownMenu/DropdownMenu";
import { useNavigate } from "react-router-dom";
import { createPortal } from "react-dom";
import Footer from "../../Components/Footer/Footer";
import Button from "../../Components/Buttons/Button";
import { NEW_PLANT_ROUTE } from "../../utils/routesAndEndpoints/routesAndEndpoints";

function Home() {
  const {
    getPlants,
    isLoadingPlants,
    plants,
    totalPlants,
    currentPage,
    setCurrentPage,
  } = useFetchPlants();
  const observerRef = useRef<HTMLDivElement>(null);
  const limit = 10;

  const [isList, setIsList] = useState(false);
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [sortBy, setSortBy] = useState<string | "">("");
  const [isDescending, setIsDescending] = useState<boolean>(false);
  const navigate = useNavigate();

  useEffect(() => {
    getPlants(currentPage, limit);
  }, [currentPage]);

  const handleIntersect = useCallback(
    (entries: IntersectionObserverEntry[]) => {
      const [entry] = entries;

      if (
        entry.isIntersecting &&
        plants.length < totalPlants &&
        !isLoadingPlants
      ) {
        console.log("Loading more plants...");
        setCurrentPage((prev) => prev + 1);
      }
    },
    [plants.length, totalPlants, isLoadingPlants, setCurrentPage]
  );

  useEffect(() => {
    const observer = new IntersectionObserver(handleIntersect, {
      root: null,
      rootMargin: "0px",
      threshold: 1.0,
    });

    if (observerRef.current) observer.observe(observerRef.current);

    return () => {
      if (observerRef.current) observer.unobserve(observerRef.current);
    };
  }, [handleIntersect]);

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

  const handleNavigatePlant = () => {
    navigate(NEW_PLANT_ROUTE);
  };

  const filteredPlants = plants
    ?.filter(
      (plant) =>
        plant.name?.toLowerCase().includes(searchQuery) ||
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
      <div className={classes.homeContainer}>
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
                className={
                  sortBy ? classes.orderCard : classes.orderCardDisabled
                }
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
            <div
              className={isList ? classes.plantRowList : classes.plantRowGrid}
            >
              {filteredPlants &&
                filteredPlants.length > 0 &&
                filteredPlants.map((plant, index) => (
                  <div key={index}>
                    <div key={plant._id}>
                      <PlantCard
                        id={plant._id}
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
                ))}
            </div>
            {filteredPlants &&
              filteredPlants.length === 0 &&
              !isLoadingPlants && <p>No plants to show!</p>}
            {isLoadingPlants && <Spinner />}
          </div>
          <div ref={observerRef} className={classes.lazyLoadTrigger}></div>
        </div>
        <div className={classes.footerContainer}>
          <Footer>
            <Button variant="tertiary" onClick={handleNavigatePlant}>
              New Plant
            </Button>
          </Footer>
        </div>
      </div>
    </div>
  );
}

export default Home;

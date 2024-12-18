import { useEffect } from "react";
import { useFetchPlantDetails } from "../../hooks/useFetchPlantDetails";
import classes from "./Plant.module.css";
import { useLocation } from "react-router-dom";
import Spinner from "../../Components/Spinner/Spinner";

function Plant() {
  const { getPlantData, plantData, isLoadingPlantData } =
    useFetchPlantDetails();
  const location = useLocation();
  const id = location.state.id;

  useEffect(() => {
    getPlantData(id);
  }, []);

  return (
    <div className={classes.mainContainer}>
      <h1>Plant Details</h1>
      <p>Plant Name: {plantData?.name}</p>
      {isLoadingPlantData && <Spinner />}
    </div>
  );
}

export default Plant;

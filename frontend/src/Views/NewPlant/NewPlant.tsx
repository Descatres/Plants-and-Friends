import classes from "./NewPlant.module.css";
import Spinner from "../../Components/Spinner/Spinner";
import { useCreatePlant } from "../../hooks/useCreatePlant";

function Plant() {
  const { createPlant } = useCreatePlant();

  return (
    <div className={classes.mainContainer}>
      <h1>Plant Creation</h1>
    </div>
  );
}

export default Plant;

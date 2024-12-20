import classes from "./PlantCard.module.css";
import defaultPlantImage from "../../assets/defaultPlantImage.svg";
import { useNavigate } from "react-router-dom";
import { PLANT_ROUTE } from "../../utils/routesAndEndpoints/routesAndEndpoints";

type PlantCardProps = {
  id?: string;
  name?: string;
  species?: string;
  minTemperature?: number;
  maxTemperature?: number;
  minHumidity?: number;
  maxHumidity?: number;
  imageUrl?: string;
  isList?: boolean;
};

function PlantCard({
  id,
  name,
  species,
  minTemperature,
  maxTemperature,
  minHumidity,
  maxHumidity,
  imageUrl,
  isList = false,
}: PlantCardProps) {
  const navigate = useNavigate();
  const handleNavigatePlant = () => {
    navigate(PLANT_ROUTE, { state: { id } });
  };
  return (
    <div
      key={id}
      className={`${classes.plantCard} ${isList ? classes.list : classes.grid}`}
      onClick={handleNavigatePlant}
    >
      <div className={classes.plantCardImage}>
        <img src={imageUrl ? imageUrl : defaultPlantImage} alt={name} />
      </div>
      <div className={classes.plantCardDetails}>
        {isList && <h2>{name}</h2>}
        {!isList && <p>{name}</p>}
        {isList && (
          <div className={classes.plantCardExtra}>
            <p>{species}</p>
            <p>
              Temperature: {minTemperature ?? "-"}°C - {maxTemperature ?? "-"}°C
            </p>
            <p>
              Humidity: {minHumidity ?? "-"}% - {maxHumidity ?? "-"}%
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

export default PlantCard;

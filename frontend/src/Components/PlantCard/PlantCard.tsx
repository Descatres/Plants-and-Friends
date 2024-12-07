import classes from "./PlantCard.module.css";
import defaultPlantImage from "../../assets/defaultPlantImage.svg";

type PlantCardProps = {
  id: number;
  name: string;
  image?: string;
  species: string;
  temperature: { min: number; max: number };
  humidity: { min: number; max: number };
  isList?: boolean;
};

function PlantCard({
  id,
  name,
  image,
  species,
  temperature,
  humidity,
  isList = false,
}: PlantCardProps) {
  return (
    <div
      className={`${classes.plantCard} ${isList ? classes.list : classes.grid}`}
    >
      <div className={classes.plantCardImage}>
        <img src={image ? image : defaultPlantImage} alt={name} />
      </div>
      <div className={classes.plantCardDetails}>
        {isList && <h2>{name}</h2>}
        {!isList && <p>{name}</p>}
        {isList && (
          <div className={classes.plantCardExtra}>
            <p>{species}</p>
            <p>
              Temperature: {temperature?.min}°C - {temperature?.max}°C
            </p>
            <p>
              Humidity: {humidity?.min}% - {humidity?.max}%
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

export default PlantCard;

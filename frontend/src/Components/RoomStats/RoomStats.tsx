import classes from "./RoomStats.module.css";
import humidityIcon from "../../assets/humidity.svg";
import temperatureIcon from "../../assets/temperature.svg";
import { useState } from "react";

function RoomStats() {
  const [temperature, setTemperature] = useState<number | null>(null);
  const [humidity, setHumidity] = useState<number | null>(null);

  // TODO fetch temperature and humidity from the backend

  return (
    <div className={classes.mainContainer}>
      <div className={classes.statsContainer}>
        <img className={classes.icon} src={temperatureIcon}></img>
        <p className={classes.temperature}>{temperature ?? "-"}ºC</p>
      </div>
      <div className={classes.statsContainer}>
        <img className={classes.icon} src={humidityIcon}></img>
        <p className={classes.humidity}>{humidity ?? "-"}%</p>
      </div>
    </div>
  );
}

export default RoomStats;

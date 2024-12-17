import classes from "./RoomStats.module.css";
import humidityIcon from "../../assets/humidity.svg";
import temperatureIcon from "../../assets/temperature.svg";
import { useEffect, useState } from "react";
import { useFetchRoomStats } from "../../hooks/useFetchRoomStats";

function RoomStats() {
  const { temperature, humidity, getTemperature, getHumidity } =
    useFetchRoomStats();

  // useEffect(() => {
  //   getTemperature();
  //   getHumidity();

  //   const interval = setInterval(() => {
  //     getTemperature();
  //     getHumidity();
  //   }, 300000);

  //   return () => clearInterval(interval);
  // }, [getTemperature, getHumidity]);

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

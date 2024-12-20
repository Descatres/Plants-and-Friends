import { useEffect, useState } from "react";
import { useFetchPlantDetails } from "../../hooks/useFetchPlantDetails";
import classes from "./PlantDetails.module.css";
import { useLocation, useNavigate } from "react-router-dom";
import Spinner from "../../Components/Spinner/Spinner";
import cameraIcon from "../../assets/camera.svg";
import RoomStats from "../../Components/RoomStats/RoomStats";
import Footer from "../../Components/Footer/Footer";
import Button from "../../Components/Buttons/Button";
import { HOME_ROUTE } from "../../utils/routesAndEndpoints/routesAndEndpoints";
import { useUpdatePlant } from "../../hooks/useUpdatePlant";
import { useDeletePlant } from "../../hooks/useDeletePlant";

function PlantDetails() {
  const { getPlantData, plantData, isLoadingPlantData } =
    useFetchPlantDetails();
  const location = useLocation();
  const id = location.state.id;

  const { updatePlant } = useUpdatePlant();
  const { deletePlant } = useDeletePlant();
  const navigate = useNavigate();

  const [plantImage, setPlantImage] = useState<string>("");
  const [plantName, setPlantName] = useState<string>("");
  const [plantSpecies, setPlantSpecies] = useState<string>("");
  const [plantDescription, setPlantDescription] = useState<string>("");
  const [plantMaxTemperature, setPlantMaxTemperature] = useState<number>(0);
  const [plantMinTemperature, setPlantMinTemperature] = useState<number>(0);
  const [plantMaxHumidity, setPlantMaxHumidity] = useState<number>(0);
  const [plantMinHumidity, setPlantMinHumidity] = useState<number>(0);

  const [errorMinTemp, setErrorMinTemp] = useState<string>("");
  const [errorMaxTemp, setErrorMaxTemp] = useState<string>("");
  const [errorMinHum, setErrorMinHum] = useState<string>("");
  const [errorMaxHum, setErrorMaxHum] = useState<string>("");

  const [isModified, setIsModified] = useState<boolean>(false);

  useEffect(() => {
    getPlantData(id);
  }, []);

  useEffect(() => {
    if (plantData) {
      setPlantName(plantData.name || "");
      setPlantSpecies(plantData.species || "");
      setPlantDescription(plantData.description || "");
      setPlantMaxTemperature(plantData.maxTemperature || 0);
      setPlantMinTemperature(plantData.minTemperature || 0);
      setPlantMaxHumidity(plantData.maxHumidity || 0);
      setPlantMinHumidity(plantData.minHumidity || 0);
      setPlantImage(plantData.imageUrl || "");
    }
  }, [plantData]);

  useEffect(() => {
    if (plantData) {
      const isChanged =
        plantName !== plantData.name ||
        plantSpecies !== plantData.species ||
        plantDescription !== plantData.description ||
        plantMaxTemperature !== plantData.maxTemperature ||
        plantMinTemperature !== plantData.minTemperature ||
        plantMaxHumidity !== plantData.maxHumidity ||
        plantMinHumidity !== plantData.minHumidity ||
        plantImage !== plantData.imageUrl;

      setIsModified(isChanged);
    }
  }, [
    plantName,
    plantSpecies,
    plantDescription,
    plantMaxTemperature,
    plantMinTemperature,
    plantMaxHumidity,
    plantMinHumidity,
    plantImage,
    plantData,
  ]);

  const handlePlantName = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPlantName(e.target.value);
  };

  const handlePlantSpecies = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPlantSpecies(e.target.value);
  };

  const handlePlantDescription = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPlantDescription(e.target.value);
  };

  const handlePlantMaxTemperature = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = Number(e.target.value);
    if (value < 0 || value > 100) {
      setErrorMaxTemp("Max temperature must be between 0 and 100");
    } else if (value < plantMinTemperature) {
      setErrorMaxTemp(
        "Max temperature must be greater than or equal to Min temperature"
      );
    } else {
      setErrorMaxTemp("");
      setErrorMinTemp("");
      setPlantMaxTemperature(value);
    }
  };

  const handlePlantMinTemperature = (
    e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const value = Number(e.target.value);
    if (value < 0 || value > 100) {
      setErrorMinTemp("Min temperature must be between 0 and 100");
    } else if (value > plantMaxTemperature) {
      setErrorMinTemp(
        "Min temperature must be less than or equal to Max temperature"
      );
    } else {
      setErrorMinTemp("");
      setErrorMaxTemp("");
      setPlantMinTemperature(value);
    }
  };

  const handlePlantMaxHumidity = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = Number(e.target.value);
    if (value < 0 || value > 100) {
      setErrorMaxHum("Max humidity must be between 0 and 100");
    } else if (value < plantMinHumidity) {
      setErrorMaxHum(
        "Max humidity must be greater than or equal to Min humidity"
      );
    } else {
      setErrorMaxHum("");
      setErrorMinHum("");
      setPlantMaxHumidity(value);
    }
  };

  const handlePlantMinHumidity = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = Number(e.target.value);
    if (value < 0 || value > 100) {
      setErrorMinHum("Min humidity must be between 0 and 100");
    } else if (value > plantMaxHumidity) {
      setErrorMinHum("Min humidity must be less than or equal to Max humidity");
    } else {
      setErrorMinHum("");
      setErrorMaxHum("");
      setPlantMinHumidity(value);
    }
  };

  const handlePlantImage = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files![0];
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => {
      setPlantImage(reader.result as string);
    };
  };

  const handleCreatePlant = () => {
    updatePlant({
      _id: id,
      name: plantName,
      species: plantSpecies,
      description: plantDescription,
      maxTemperature: plantMaxTemperature,
      minTemperature: plantMinTemperature,
      maxHumidity: plantMaxHumidity,
      minHumidity: plantMinHumidity,
      imageUrl: plantImage,
      lastUpdate: new Date().toLocaleString(),
    });
    navigate(HOME_ROUTE);
  };

  const handleDeletePlant = () => {
    deletePlant(id);
    navigate(HOME_ROUTE);
  };

  return isLoadingPlantData ? (
    <Spinner />
  ) : (
    <div className={classes.mainContainer}>
      <div className={classes.container}>
        <div className={classes.content}>
          <div className={classes.roomStatsContainer}>
            <RoomStats />
          </div>
          {
            <div className={classes.plantOptionsContainer}>
              <div className={classes.plantInfoContainer}>
                <div className={classes.plantImageContainer}>
                  <img src={cameraIcon} alt="camera icon" />
                </div>

                <div className={classes.plantMainInfoContainer}>
                  <h1>Name</h1>
                  <input
                    type="text"
                    placeholder="Plant Name"
                    value={plantName}
                    onChange={handlePlantName}
                  />
                  <h1>Species</h1>
                  <input
                    type="text"
                    placeholder="Plant Species"
                    value={plantSpecies}
                    onChange={handlePlantSpecies}
                  />
                </div>

                <div className={classes.plantDescriptionContainer}>
                  <h1>Description</h1>
                  <input
                    type="text"
                    placeholder="Plant Description"
                    value={plantDescription}
                    onChange={handlePlantDescription}
                  />
                </div>
              </div>

              <div className={classes.plantStatsContainer}>
                <div className={classes.plantTemperatureContainer}>
                  <div className={classes.plantTemperature}>
                    <h1>Ideal Temperature</h1>
                  </div>
                  <div className={classes.plantTemperatureSlider}>
                    <input
                      type="number"
                      min="0"
                      value={plantMinTemperature}
                      onChange={handlePlantMinTemperature}
                    />
                    {errorMinTemp && <p>{errorMinTemp}</p>}
                    <input
                      type="number"
                      max="100"
                      value={plantMaxTemperature}
                      onChange={handlePlantMaxTemperature}
                    />
                    {errorMaxTemp && <p>{errorMaxTemp}</p>}
                  </div>
                </div>
                <div className={classes.plantHumidityContainer}>
                  <div className={classes.plantHumidity}>
                    <h1>Ideal Humidity</h1>
                  </div>
                  <div className={classes.plantHumiditySlider}>
                    <input
                      type="number"
                      value={plantMinHumidity}
                      onChange={handlePlantMinHumidity}
                    />
                    {errorMinHum && <p>{errorMinHum}</p>}
                    <input
                      type="number"
                      value={plantMaxHumidity}
                      onChange={handlePlantMaxHumidity}
                    />
                    {errorMaxHum && <p>{errorMaxHum}</p>}
                  </div>
                </div>
              </div>
            </div>
          }
        </div>
        <div className={classes.footerContainer}>
          <Footer>
            <div style={{ display: "flex", gap: "1rem" }}>
              <Button
                variant="tertiary"
                onClick={handleCreatePlant}
                disabled={!isModified}
              >
                Save Plant
              </Button>
              <Button variant="danger" onClick={handleDeletePlant}>
                Delete Plant
              </Button>
            </div>
          </Footer>
        </div>
      </div>
    </div>
  );
}

export default PlantDetails;

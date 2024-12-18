import { useNavigate } from "react-router-dom";
import Button from "../Buttons/Button";
import classes from "./Footer.module.css";
import { NEW_PLANT_ROUTE } from "../../utils/routesAndEndpoints/routesAndEndpoints";

function Footer() {
  const navigate = useNavigate();

  const handleNavigate = () => {
    navigate(NEW_PLANT_ROUTE);
  };

  return (
    <footer className={classes.footer}>
      <Button variant="tertiary" onClick={handleNavigate}>
        New Plant
      </Button>
    </footer>
  );
}

export default Footer;

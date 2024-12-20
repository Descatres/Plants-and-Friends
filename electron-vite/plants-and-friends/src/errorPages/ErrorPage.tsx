import { useNavigate } from "react-router-dom";
import classes from "./ErrorPage.module.css";
import { HOME_ROUTE } from "../utils/routesAndEndpoints/routesAndEndpoints";
import Button from "../Components/Buttons/Button";

type ErrorPageProps = {
  error?: string;
  message?: string;
};

function ErrorPage({ error, message }: ErrorPageProps) {
  const handleGoHome = () => {
    // refresh and go home
    window.location.href = HOME_ROUTE;
  };

  return (
    <div className={classes.container}>
      <h1>{error ?? "An error occurred!"}</h1>
      <p>{message ?? "Something went wrong :("}</p>
      <Button variant="primary" onClick={handleGoHome}>
        Go Home
      </Button>
    </div>
  );
}

export default ErrorPage;

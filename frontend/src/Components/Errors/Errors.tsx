import classes from "./Errors.module.css";

function Error() {
  return (
    <div className={classes.errorContainer}>
      <h2>Error. Try reloading the page</h2>
    </div>
  );
}

export default Error;

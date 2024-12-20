import { ReactNode } from "react";
import classes from "./LoggedOutBase.module.css";
import PineModel from "../../renders/PineModel";

function LoggedOutBaseContent({ children }: { children: ReactNode }) {
  return (
    <div className={classes.background}>
      <div className={classes.contentContainer}>
        <div className={classes.formParentContainer}>
          <div className={classes.formContainer}>
            <div className={classes.form}>{children}</div>
          </div>
        </div>
        <div className={classes.treeContainer}>
          <div className={classes.tree}>
            <PineModel />
          </div>
        </div>
      </div>
    </div>
  );
}

export default LoggedOutBaseContent;

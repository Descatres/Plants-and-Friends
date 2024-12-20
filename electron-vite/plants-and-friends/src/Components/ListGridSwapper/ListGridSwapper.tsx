import gridIcon from "../../assets/Grid.svg";
import listIcon from "../../assets/list.svg";
import classes from "./ListGridSwapper.module.css";

type ListGridSwapperProps = {
  isList: boolean;
  handleIsList?: () => void;
  handleIsGrid?: () => void;
};

function ListGridSwapper({
  isList,
  handleIsList,
  handleIsGrid,
}: ListGridSwapperProps) {
  return (
    <div className={classes.mainContainer}>
      <img
        src={gridIcon}
        alt="grid"
        onClick={handleIsGrid}
        className={`${!isList ? classes.selected : ""}`}
      />
      <img
        src={listIcon}
        alt="list"
        onClick={handleIsList}
        className={`${isList ? classes.selected : ""}`}
      />
    </div>
  );
}

export default ListGridSwapper;

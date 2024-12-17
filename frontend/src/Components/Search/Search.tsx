import classes from "./Search.module.css";
import SearchIcon from "../../assets/Search.svg";

function Search() {
  return (
    <div className={classes.mainContainer}>
      <input type="text" placeholder="Search plant" />
      <img src={SearchIcon} alt="search" />
    </div>
  );
}

export default Search;

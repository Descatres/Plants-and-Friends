import classes from "./Search.module.css";
import SearchIcon from "../../assets/Search.svg";

type SearchProps = {
  onSearch: (query: string) => void;
};

function Search({ onSearch }: SearchProps) {
  return (
    <div className={classes.mainContainer}>
      <input
        type="text"
        placeholder="Search plant"
        onChange={(e) => onSearch(e.target.value)}
      />
      <img src={SearchIcon} alt="search" />
    </div>
  );
}

export default Search;

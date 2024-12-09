import { Outlet } from "react-router-dom";
import LoggedOutBaseContent from "../../Views/LoggedOutBase/LoggedOutBase";

function LoggedOutWrapper() {
  return (
    <LoggedOutBaseContent>
      <Outlet />
    </LoggedOutBaseContent>
  );
}

export default LoggedOutWrapper;

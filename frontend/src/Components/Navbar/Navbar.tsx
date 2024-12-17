import { useState } from "react";
import classes from "./Navbar.module.css";
import Button from "../Buttons/Button";
import bell from "../../assets/bell.svg";
import menu from "../../assets/menu.svg";
import { useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../../store/store";
import DropdownMenu from "../DropdownMenu/DropdownMenu";
import { removeToken } from "../../store/slices/tokenSlice";
import { toast } from "react-toastify";
import {
  HOME_ROUTE,
  LANDING_PAGE_ROUTE,
  LOGIN_ROUTE,
  REGISTER_ROUTE,
  ROOM_ALERTS_ROUTE,
} from "../../utils/routesAndEndpoints/routesAndEndpoints";

function Navbar() {
  const navigate = useNavigate();
  const token = useSelector((state: RootState) => state.token.value);
  const dispatch = useDispatch();
  const handleNavigateLogin = () => {
    navigate(LOGIN_ROUTE);
  };

  const handleNavigateRegister = () => {
    navigate(REGISTER_ROUTE);
  };

  const handleNavigateHome = () => {
    if (token) navigate(HOME_ROUTE);
    else navigate(LANDING_PAGE_ROUTE);
  };

  const handleLogout = () => {
    toast.success("See ya later!");
    dispatch(removeToken(null));
    navigate("/");
  };

  const mockNotifications = [
    { id: 1, name: "Notification 1", onClick: () => {} },
    { id: 2, name: "Notification 2", onClick: () => {} },
    { id: 3, name: "Notification 3", onClick: () => {} },
  ];

  const removeNotification = (id: number) => {
    mockNotifications.filter((notification) => notification.id !== id);
  };

  return (
    <div className={classes.navbar}>
      <div className={classes.logoTitle}>
        <img
          onClick={handleNavigateHome}
          style={{ cursor: "pointer" }}
          src="/plant.svg"
          alt="plant"
          width="50px"
        />
        <h1 onClick={handleNavigateHome} style={{ cursor: "pointer" }}>
          Plants&Friends
        </h1>
      </div>
      {token ? (
        <div className={classes.buttons}>
          <DropdownMenu
            button={
              <img
                src={bell}
                className={classes.buttonWrapper}
                alt="Notifications"
                width="30px"
                unselectable="on"
              />
            }
            options={[
              {
                id: 1,
                name: "Notification 1",
                onClick: () => removeNotification(1),
              },
              {
                id: 2,
                name: "Notification 2",
                onClick: () => removeNotification(2),
              },
              {
                id: 3,
                name: "Notification 3",
                onClick: () => removeNotification(3),
              },
            ]}
          />
          <DropdownMenu
            button={
              <img
                src={menu}
                className={classes.buttonWrapper}
                alt="Menu"
                width="30px"
                unselectable="on"
              />
            }
            options={[
              {
                id: 2,
                name: "Room Alerts",
                onClick: () => navigate(ROOM_ALERTS_ROUTE),
              },
              { id: 3, name: "Logout", onClick: handleLogout },
            ]}
          />
        </div>
      ) : (
        <Button
          onClick={
            window.location.pathname !== "/login"
              ? handleNavigateLogin
              : handleNavigateRegister
          }
        >
          {window.location.pathname !== "/login" ? "Login" : "Register"}
        </Button>
      )}
    </div>
  );
}

export default Navbar;

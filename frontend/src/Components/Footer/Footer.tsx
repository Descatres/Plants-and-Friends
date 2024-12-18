import Button from "../Buttons/Button";
import classes from "./Footer.module.css";

function Footer() {
  return (
    <footer className={classes.footer}>
      <Button variant="tertiary">New Plant</Button>
    </footer>
  );
}

export default Footer;

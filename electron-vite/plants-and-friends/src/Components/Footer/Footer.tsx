import classes from "./Footer.module.css";

function Footer({ children }: any) {
  return <footer className={classes.footer}>{children}</footer>;
}

export default Footer;

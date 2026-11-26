import type { ReactNode } from "react";
import clsx from "clsx";
import styles from "./styles.module.css";

import Logo from "@site/static/img/logo3.svg";

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className={clsx("row", styles.row)}>
          <div className={clsx("col col--4", styles.logoCol)}>
            <Logo
              className={styles.featureSvg}
              role="img"
              aria-label="OpenUGC logo"
            />
          </div>
          <div className={clsx("col col--8", styles.intro)}>
            <p>
              OpenUGC is an open-source <strong>REST API</strong> for
              user-generated content (<strong>UGC</strong>). It sits between
              your <strong>game or app</strong>, a{" "}
              <strong>PostgreSQL database</strong> and a{" "}
              <strong>file storage</strong>, and gives you everything needed to
              let players upload, browse, rate and download content.
            </p>
            <p>
              It isn't tied to any engine. Anything that can make HTTP requests
              can use it.
            </p>
            <p>
              Built with Java Spring Boot, the backend is modular, scalable and
              easy to extend, so you can add your own features or integrations.
            </p>
          </div>
        </div>
      </div>
    </section>
  );
}

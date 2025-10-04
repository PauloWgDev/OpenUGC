import type {ReactNode} from 'react';
import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

type FeatureItem = {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>> | null;
  description: ReactNode;
};

const FeatureList: FeatureItem[] = [
  {
    title: 'Full Control for Developers',
    Svg: require('@site/static/img/logo3.svg').default,
    description: (
      <>
        OpenUGC is built to give you maximum flexibility. 
        You decide how and where to host, freely adapt the codebase,
        and shape UGC to fit your project’s vision.
      </>
    ),
  },
  {
    title: 'Powered by Java SpringBoot',
    Svg: require('@site/static/img/logo3.svg').default,
    description: (
      <>
        Powered by Java Spring Boot, the backend is modular, scalable,
        and easy to extend. Add custom features or integrations with ease. 
      </>
    ),
  },
  {
    title: 'Unity Integration in Minutes',
    Svg: require('@site/static/img/logo3.svg').default,
    description: (
      <>
        With <b>U3GC</b>, our Unity client library, you can bring
        UGC into your game in just a few lines of code—fast setup,
        smooth integration, and ready to scale.
      </>
    ),
  },
];

function Feature({title, Svg, description}: FeatureItem) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        {Svg && <Svg className={styles.featureSvg} role="img" />}
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures(): ReactNode {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}

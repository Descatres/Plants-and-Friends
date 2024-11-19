import { SetStateAction, useEffect, useState } from "react";
import { Canvas, useFrame } from "@react-three/fiber";
import { OrbitControls } from "@react-three/drei";
import { GLTFLoader } from "three/examples/jsm/loaders/GLTFLoader";
import classes from "./LandingPage.module.css";
import * as THREE from "three";
import Button from "../../Components/Buttons/Button";

function LandingPage() {
  return (
    <div className={classes.background}>
      <div className={classes.contentContainer}>
        <div className={classes.treeContainer}>
          <div className={classes.formContainer}>
            <div className={classes.form}>
              <h1>Rooted in Care Growing with Precision</h1>
              <p>
                Plant pantry that tracks soil humidity, temperature, and plant
                details, offering real-time insights for optimal growth.
              </p>
              <div className={classes.buttonsContainer}>
                <>
                  <Button variant="secondary">Login</Button>
                  <p>or</p>
                  <p>Register</p>
                </>
              </div>
            </div>
          </div>
          <div className={classes.tree}>
            <Canvas>
              <ambientLight intensity={2.5} />
              <directionalLight position={[1000, 1000, 1000]} />
              <PineModel />
              <OrbitControls />
            </Canvas>
          </div>
        </div>
      </div>
    </div>
  );
}

export default LandingPage;

function PineModel() {
  const [model, setModel] = useState<THREE.Object3D | null>(null);

  useFrame(() => {
    if (model) {
      model.rotation.y += 0.001;
    }
  });

  useEffect(() => {
    const loader = new GLTFLoader();
    loader.load(
      new URL("../../assets/Pine.glb", import.meta.url).href,
      (gltf: any) => {
        setModel(gltf.scene);
      }
    );
  }, []);

  if (!model) return null;

  return <primitive object={model} scale={3} />;
}

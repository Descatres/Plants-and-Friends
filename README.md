# Plants&Friends

### Plants compendium able to save and show plants:
- Names
- Species
- Image of the plants
- Descriptions
### And able to track:
- temperature (of the room/place the plants are placed)
- humidity (of the room/place the plants are placed)

### Other features
- Register/Login
- It is possible to create an online backup to Firestore to be able to restore all the data for each plant on, for example, a new device
- Alerts can be sent (notifications) if the value read by any of the sensors is outside the set interval by the user
- It is possible to search for each plant by name or species

---

- You can check the mockups [here](https://www.figma.com/file/R5EZiiwMCeeSawiDweYcR1/Plants%26Friends?type=design&node-id=0%3A1&mode=design&t=IOr0niK1qOHFxdOd-1)

---

## How to run
- Fork the repo and run it on Android Studio
- The app can be run by changing the Firestore account with [this tutorial](https://firebase.google.com/docs/firestore/quickstart#:~:text=If%20you%20haven%27t%20already%2C%20create%20a%20Firebase%20project%3A%20In%20the%20Firebase%20console%2C%20click%20Add%20project%2C%20then%20follow%20the%20on%2Dscreen%20instructions%20to%20create%20a%20Firebase%20project%20or%20to%20add%20Firebase%20services%20to%20an%20existing%20GCP%20project.)
- You can run [this script](https://wokwi.com/projects/385292642174231553) to simulate the Arduino with the sensors emitting data to your device through MQTT

---

## Examples
- Login/Register

![image](https://github.com/Descatres/Plants-and-Friends/assets/73725403/1772394a-873d-445e-a7e9-0d22ad0ecb03)


- Homepage
  
![image](https://github.com/Descatres/Plants-and-Friends/assets/73725403/f4c2996d-101e-4581-8596-2728080b0386)

- Add/Edit plant

![image](https://github.com/Descatres/Plants-and-Friends/assets/73725403/048e4b91-a65e-4593-94f2-0d11c5fe507c)

- Alerts page

![image](https://github.com/Descatres/Plants-and-Friends/assets/73725403/2b6d8651-8bc4-4bfd-8816-49cfeb02eb97)

---

### Notes
- All the data received from the sensors is sent through MQTT
- The sensors feature only works until Android 12.
- The images are being backed up to Firestore but it is not possible, yet, to download them.
- _This project was made for a university assignment (the final project of that class) and the grade was 9.3/10. Some things might be missing due to the orientations given and/or time restrictions._

---

_Made, with love, for Android (with Java)_

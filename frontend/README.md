# PlantsPantry

- A simple App to manage your plants.

# How to run

`npm i`
`npm run dev`

- The history state is automatically handled by react-router and react-router-dom.
- Lazy loading is being applied to fetch the plants (not working - it broke at the last minute :/).

- Some UX improvements could've been made, such as making some buttons disabled (for example, to update the plant, the user must first update a field, only then the button should be enabled); modals to confirm destructive non reversible actions, such as delting a plant; and so on. We know this would be the best approach but, due to lack of time and not using a UI library, like Chakra UI (which is lovely in our opinion), we couldn't implement it in time. But we do know it is something we can improve the app on!

- The images are not being set because, once again, we did not have time.

- The notifications got broken at some point and we couldn't fix it in time. Nonetheless, only the 10 most recent notifications would be shown, as this is supposed to be a web app to control the plants and check some notifications. For more in depth usage, the Android App we have is a much better option. This PoC is just for basic plant management with some integrations to get sensor data.

- The same species plants are not being shown on the plant details page but it can be filtered by the user in the homepage with the sort by and search features! This would have lazy loading as well if we were to implement it.

- Lastly, the MQTT integration is working, which is a ver nice feature. It only works for one sensor (this is supposed to be a PoC), but it can be upscaled to multiple by giving ids to the sensor in the script and associate each id to the plant or room the user wants.

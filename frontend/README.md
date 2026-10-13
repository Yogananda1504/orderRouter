# React admin allocation monitor

The dashboard is read-only. Simulated users submit an order every 5 seconds,
up to 30 new orders per application run. Allocation runs automatically using
each user's delivery choice. The dashboard polls every 2 seconds to show
assignments and stock changes. Restart the backend to replay the simulation.
Set `demo.simulation.enabled=false` to disable automatic order creation.

Start `OrderApplication` in the IDE, then open http://localhost:8081.
The React production build is included in Spring Boot's static resources.

For frontend development, run these commands from `frontend`:

```powershell
npm install
npm run dev
```

Open http://localhost:5173. Vite forwards `/api` requests to the backend on port 8081.
Keep `OrderApplication` running for data and allocation actions.

After frontend edits, run `npm run build`, then restart the backend so it reloads
the bundled resources. The build writes to `order/src/main/resources/static`.

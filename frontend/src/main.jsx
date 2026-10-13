import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

async function request() {
  let response;
  try {
    response = await fetch('/api/demo');
  } catch {
    throw new Error('Cannot reach the backend. Start OrderApplication on port 8081 and try Refresh.');
  }
  if (!response.ok) {
    throw new Error(`The backend returned ${response.status}. Check that OrderApplication is running on port 8081, then try Refresh.`);
  }
  try { return await response.json(); }
  catch { throw new Error('The API did not return JSON. Check that the backend is running on port 8081.'); }
}

function Stock({ items }) {
  return <div className="stock">{Object.entries(items).map(([id, quantity]) =>
    <span key={id}><span className="muted">#{id}</span> <strong>{quantity}</strong></span>)}</div>;
}

function App() {
  const [data, setData] = useState(null);
  const [busy, setBusy] = useState(true);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    let timer;
    async function poll() {
      try {
        const value = await request();
        if (active) { setData(value); setError(''); setLastUpdated(new Date()); }
      } catch (err) { if (active) setError(err.message); }
      finally {
        if (active) { setBusy(false); timer = setTimeout(poll, 2000); }
      }
    }
    poll();
    return () => { active = false; clearTimeout(timer); };
  }, []);

  const users = Object.entries(data?.users ?? {});
  const orders = Object.values(data?.orders ?? {}).sort((a, b) => b.id - a.id);
  const centers = Object.values(data?.centers ?? {});
  const assigned = orders.filter(order => order.status === 'ASSIGNED').length;
  const owners = Object.fromEntries(users.flatMap(([id, user]) => user.orders.map(order => [order.id, id])));

  return <div className="layout">
    <aside className="sidebar">
      <a className="brand" href="/" aria-label="Order Router home"><span className="brand-icon">OR</span> Order Router</a>
      <p className="nav-label">ADMIN MONITOR</p>
      <a className="nav active" href="#overview">Live allocations <span>01</span></a>
      <a className="nav" href="#users">User activity</a>
      <a className="nav" href="#centers">Warehouse stock</a>
      <div className="sidebar-note"><span className="dot" /> User simulation<p>Users place orders automatically. This dashboard monitors the results.</p></div>
    </aside>
    <main id="overview">
      <header className="topbar"><span>Admin <span className="muted">/</span> Allocation monitor</span><span className="demo-tag">LIVE SIMULATION</span></header>
      <div className="heading"><div><p className="eyebrow">DELIVERY OPERATIONS</p><h1>Watch orders find a warehouse.</h1><p>Monitor automatic assignments, user preferences, and remaining stock.</p></div>
        <span className="count">Read-only monitor</span>
      </div>
      <div className="metrics">
        <div><span>Total orders</span><strong>{data ? orders.length : '—'}</strong></div>
        <div><span>Assigned</span><strong>{data ? assigned : '—'}<small> ready for delivery</small></strong></div>
        <div><span>Unassigned</span><strong>{data ? orders.length - assigned : '—'}</strong></div>
        <div><span>Active centers</span><strong>{data ? centers.length : '—'}</strong></div>
      </div>
      <div className={'notice' + (error ? ' error' : '')} role={error ? 'alert' : 'status'} aria-live="polite">
        <span>{error || (busy ? 'Connecting to simulation...' : data.simulatedOrders < data.simulationLimit
          ? `Users create an order every 5 seconds. ${data.simulatedOrders} of ${data.simulationLimit} simulated orders received.`
          : `Simulation complete: ${data.simulatedOrders} orders received. Monitoring final allocations.`)}</span>
        <span>{lastUpdated ? `Updated ${lastUpdated.toLocaleTimeString()}` : 'Connecting...'}</span>
      </div>
      <section id="users"><div className="section-heading"><div><h2>User activity</h2><p>Preferences are selected by users; the admin monitors their allocations.</p></div><span className="count">{users.length} users</span></div>
        <div className="users-grid">{users.map(([id, user]) => <article className="user-card" key={id}>
          <div className="user-title"><span className="avatar">U{id}</span><div><h3>User {id}</h3><p>{user.orders.length} orders</p></div></div>
          <span className="grade">{user.choice === 'COST' ? 'User chose lowest cost' : 'User chose nearest warehouse'}</span>
          <p className="strategy-detail">{user.choice === 'COST' ? 'Compares prices using warehouse grade and distance.' : 'Chooses the closest warehouse with sufficient stock.'}</p>
          <p>{user.orders.filter(order => order.status === 'ASSIGNED').length} assigned / {user.orders.filter(order => order.status !== 'ASSIGNED').length} waiting for stock</p>
        </article>)}</div>
      </section>
      <section><div className="section-heading"><div><h2>Order allocations</h2><p>Newest orders first. Assignments happen automatically as users submit orders.</p></div>
        <span className="count">{orders.length} orders</span>
      </div>
      <div className="table-wrap"><table><thead><tr><th>Order</th><th>User</th><th>Items / quantity</th><th>Delivery location</th><th>Status</th><th>Warehouse</th></tr></thead><tbody>
        {orders.map(order => <tr key={order.id}><td><strong>#{order.id}</strong></td><td>User {owners[order.id]}</td><td><Stock items={order.items} /></td><td>({order.x}, {order.y})</td><td><span className={'badge ' + order.status.toLowerCase()}>{order.status === 'ASSIGNED' ? 'Assigned' : 'Unassigned'}</span></td><td>{order.assignedCenter ? `Center ${order.assignedCenter.centerId}` : '—'}</td></tr>)}
        {!orders.length && <tr><td colSpan="6">{busy ? 'Loading orders...' : 'No orders loaded. The monitor retries automatically.'}</td></tr>}
      </tbody></table></div><div className="table-note">Item #999 has no available stock. Order #1004 demonstrates an unfulfilled order.</div></section>
      <section id="centers"><div className="section-heading"><div><h2>Warehouse availability</h2><p>Stock updates immediately after allocation.</p></div><span className="count">{centers.length} centers</span></div>
        <div className="centers-grid">{centers.map(center => <article className="center-card" key={center.centerId}><div className="center-top"><h3>Center {center.centerId}</h3><span className="grade">Grade {center.grade}</span></div><p>Location ({center.x}, {center.y}) km</p><Stock items={center.items} /></article>)}</div>
      </section>
      <footer><span>Price = grade × (10 + 10 × ceil(distance / 10))</span><span>Refreshes every 2 seconds. Restart the backend to replay the simulation.</span></footer>
    </main>
  </div>;
}

createRoot(document.getElementById('root')).render(<App />);

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import {
  TextField,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Box
} from '@mui/material';

function Orders() {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);
  const [newOrder, setNewOrder] = useState({
    region: '',
    cases: 0,
    vaccineQuantity: 0,
    expectedDeliveryTime: '',
  });

  // Fetch orders on component mount
  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    try {
      const result = await axios.get('http://localhost:8080/api/orders', {
        timeout: 5000, // Add timeout to avoid hanging
      });
      setOrders(result.data);
      setError(null);
    } catch (err) {
      if (err.response) {
        // Server responded with a status code (e.g., 404, 500)
        setError(`Error fetching orders: ${err.response.status} - ${err.response.data}`);
      } else if (err.request) {
        // Request was made, but no response received (Network Error)
        setError('Error fetching orders: Network Error. Please ensure the backend services are running. Click to retry.');
      } else {
        // Other errors
        setError('Error fetching orders: ' + err.message);
      }
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setNewOrder({ ...newOrder, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      // Validate expectedDeliveryTime format
      const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
      if (!dateRegex.test(newOrder.expectedDeliveryTime)) {
        setError('Expected Delivery Time must be in YYYY-MM-DD format');
        return;
      }
      const deliveryDate = new Date(newOrder.expectedDeliveryTime);
      const today = new Date();
      today.setHours(0, 0, 0, 0); // Reset time for comparison
      if (deliveryDate < today) {
        setError('Expected Delivery Time cannot be in the past');
        return;
      }

      const response = await axios.post('http://localhost:8080/api/orders', {
        ...newOrder,
        cases: parseInt(newOrder.cases),
        vaccineQuantity: parseInt(newOrder.vaccineQuantity),
      });
      console.log('Order Created:', response.data);
      setNewOrder({
        region: '',
        cases: 0,
        vaccineQuantity: 0,
        expectedDeliveryTime: '',
      });
      fetchOrders(); // Refresh the orders list
      setError(null);
    } catch (err) {
      if (err.response) {
        setError(`Error creating order: ${err.response.status} - ${err.response.data}`);
      } else if (err.request) {
        setError('Error creating order: Network Error. Please ensure the backend services are running.');
      } else {
        setError('Error creating order: ' + err.message);
      }
    }
  };


  return (
    <div style={{ padding: '20px' }}>
      <h1>Vaccine Order Management</h1>

      <h2>Create New Order</h2>
      <form id="order-form" onSubmit={handleSubmit}>
        <TextField
          label="Region"
          variant="outlined"
          type="text"
          name="region"
          value={newOrder.region}
          onChange={handleInputChange}
        />
        <TextField
          label="Cases"
          variant="outlined"
          type="number"
          name="cases"
          value={newOrder.cases}
          onChange={handleInputChange}
        />
        <TextField
          label="Vaccine Quantity"
          variant="outlined"
          type="number"
          name="vaccineQuantity"
          value={newOrder.vaccineQuantity}
          onChange={handleInputChange}
        />
        <TextField
          label="Expected Delivery Time (YYYY-MM-DD)"
          variant="outlined"
          type="text"
          name="expectedDeliveryTime"
          value={newOrder.expectedDeliveryTime}
          onChange={handleInputChange}
          placeholder="e.g. 2025-04-03"
        />
      </form>
      <Button variant="contained" form="order-form" type="submit" style={{ marginTop: 8}}>Create Order</Button>

      {error && (
        <p style={{ color: 'red' }}>
          {error}
          {error.includes('Network Error') && (
            <Button variant="contained" onClick={fetchOrders} style={{ marginLeft: '10px' }}>
              Retry
            </Button>
          )}
        </p>
      )}

      <h2>All Orders</h2>
      {orders.length === 0 ? (
          <p>No orders found.</p>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Region</TableCell>
                <TableCell>Cases</TableCell>
                <TableCell>Vaccine Quantity</TableCell>
                <TableCell>Expected Delivery</TableCell>
                <TableCell>Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
            {orders.map((order) => (
                <TableRow key={order.id}>
                  <TableCell>{order.id}</TableCell>
                  <TableCell>{order.region}</TableCell>
                  <TableCell>{order.cases}</TableCell>
                  <TableCell>{order.vaccineQuantity}</TableCell>
                  <TableCell>{order.expectedDeliveryTime}</TableCell>
                  <TableCell>{order.status}</TableCell>
                </TableRow>
            ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </div>
  );
}

export default Orders;
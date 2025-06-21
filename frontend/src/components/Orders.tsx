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
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Typography
} from '@mui/material';
import { useRegion } from './RegionContext.tsx';

function Orders() {
  const { region } = useRegion();
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);
  const [newOrder, setNewOrder] = useState({
    region: region,
    vaccineQuantity: 0,
    expectedDeliveryTime: '',
  });
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [sortConfig, setSortConfig] = useState({ key: 'id', direction: 'asc' });
  const [statusFilter, setStatusFilter] = useState('All');

  useEffect(() => {
    fetchOrders();
  }, []);

  const sortedOrders = [...orders]
  .filter(order => statusFilter === 'All' || order.status === statusFilter)
  .sort((a, b) => {
    const key = sortConfig.key;
    const dir = sortConfig.direction === 'asc' ? 1 : -1;
    if (a[key] < b[key]) return -1 * dir;
    if (a[key] > b[key]) return 1 * dir;
    return 0;
  });

  const handleSort = (key) => {
    setSortConfig((prev) => ({
      key,
      direction: prev.key === key && prev.direction === 'asc' ? 'desc' : 'asc',
    }));
  };

  const handleRowClick = (order) => {
    setSelectedOrder(order);
    setDialogOpen(true);
  };

  const handleCancelOrder = async () => {
    if (!selectedOrder) return;
    try {
      await axios.patch(`http://localhost:8080/api/orders/${selectedOrder.id}/status`, null, {
        params: { newStatus: 'Cancelled' },
      });
      setDialogOpen(false);
      fetchOrders();
    } catch (err) {
      setError('Failed to cancel order: ' + (err.message || err.toString()));
    }
  };

  const handleMakePriority = async () => {
    if (!selectedOrder) return;
    try {
      await axios.patch(`http://localhost:8080/api/orders/${selectedOrder.id}/status`, null, {
        params: { newStatus: 'Priority' },
      });
      setDialogOpen(false);
      fetchOrders();
    } catch (err) {
      setError('Failed to prioritize order: ' + (err.message || err.toString()));
    }
  };

  const fetchOrders = async () => {
    try {
      const result = await axios.get(`http://localhost:8080/api/orders/region/${region}`, {
        timeout: 5000,
      });
      setOrders(result.data);
      setError(null);
    } catch (err) {
      if (err.response) {
        setError(`Error fetching orders: ${err.response.status} - ${err.response.data}`);
      } else if (err.request) {
        setError('Error fetching orders: Network Error. Please ensure the backend services are running. Click to retry.');
      } else {
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
      const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
      if (!dateRegex.test(newOrder.expectedDeliveryTime)) {
        setError('Expected Delivery Time must be in YYYY-MM-DD format');
        return;
      }
      const deliveryDate = new Date(newOrder.expectedDeliveryTime);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      if (deliveryDate < today) {
        setError('Expected Delivery Time cannot be in the past');
        return;
      }

      const response = await axios.post('http://localhost:8080/api/orders', {
        ...newOrder,
        vaccineQuantity: parseInt(newOrder.vaccineQuantity),
      });
      console.log('Order Created:', response.data);
      setNewOrder({
        region: region,
        vaccineQuantity: 0,
        expectedDeliveryTime: '',
      });
      fetchOrders();
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
          label="Vaccine Quantity"
          variant="outlined"
          type="number"
          name="vaccineQuantity"
          value={newOrder.vaccineQuantity}
          onChange={handleInputChange}
        />
        <TextField
          style={{width: '300px'}}
          label="Delivery Time"
          variant="outlined"
          type="date"
          name="expectedDeliveryTime"
          value={newOrder.expectedDeliveryTime}
          onChange={handleInputChange}
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
          <Box sx={{ margin: '16px 0' }}>
            <TextField
              label="Filter by Status"
              select
              SelectProps={{ native: true }}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              {['All', 'Pending', 'Fulfilled', 'Cancelled', 'Priority'].map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </TextField>
          </Box>
          <Table>
            <TableHead>
              <TableRow>
                {['id', 'region', 'vaccineQuantity', 'expectedDeliveryTime', 'status'].map((col) => (
                  <TableCell
                    key={col}
                    onClick={() => handleSort(col)}
                    style={{ cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    {col.charAt(0).toUpperCase() + col.slice(1)}
                    {sortConfig.key === col ? (sortConfig.direction === 'asc' ? ' 🔼' : ' 🔽') : ''}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {sortedOrders.map((order) => (
                <TableRow
                  key={order.id}
                  hover
                  style={{ cursor: 'pointer' }}
                  onClick={() => handleRowClick(order)}
                >
                  <TableCell>{order.id}</TableCell>
                  <TableCell>{order.region}</TableCell>
                  <TableCell>{order.vaccineQuantity}</TableCell>
                  <TableCell>{order.expectedDeliveryTime}</TableCell>
                  <TableCell>{order.status}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>Order Actions</DialogTitle>
        <DialogContent>
          {selectedOrder && (
            <>
              <Typography><strong>ID:</strong> {selectedOrder.id}</Typography>
              <Typography><strong>Region:</strong> {selectedOrder.region}</Typography>
              <Typography><strong>Quantity:</strong> {selectedOrder.vaccineQuantity}</Typography>
              <Typography><strong>Expected Delivery:</strong> {selectedOrder.expectedDeliveryTime}</Typography>
              <Typography><strong>Status:</strong> {selectedOrder.status}</Typography>
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button
            onClick={handleCancelOrder}
            color="secondary"
            disabled={selectedOrder?.status === 'Fulfilled' || selectedOrder?.status === 'Cancelled'}
          >
            Cancel Order
          </Button>
          <Button
            onClick={handleMakePriority}
            color="primary"
            disabled={selectedOrder?.status === 'Fulfilled' || selectedOrder?.status === 'Cancelled' || selectedOrder?.status === 'Priority'}
          >
            Make Priority
          </Button>
          <Button onClick={() => setDialogOpen(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}

export default Orders;
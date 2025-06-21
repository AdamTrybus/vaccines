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
  Typography,
  Checkbox,
  TableSortLabel,
} from '@mui/material';
import { useProducer } from "./ProducerContext.tsx";

function Producers() {
  const { producer } = useProducer();
  const [orders, setOrders] = useState<any[]>([]);
  const [capacities, setCapacities] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [vaccinesQuantity, setVaccinesQuantity] = useState<string>('');
  const [productionDeadline, setProductionDeadline] = useState<string>('');
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState<string | null>(null);

  const [orderSortField, setOrderSortField] = useState('expectedDeliveryTime');
  const [orderSortDirection, setOrderSortDirection] = useState<'asc' | 'desc'>('asc');

  const [capacitySortField, setCapacitySortField] = useState('productionDeadline');
  const [capacitySortDirection, setCapacitySortDirection] = useState<'asc' | 'desc'>('asc');

  const [hideZeroExcess, setHideZeroExcess] = useState<boolean>(false);

  const orderColumns = [
    { field: 'id', label: 'ID' },
    { field: 'region', label: 'Region' },
    { field: 'vaccineQuantity', label: 'Vaccine Quantity' },
    { field: 'expectedDeliveryTime', label: 'Expected Delivery' },
    { field: 'status', label: 'Status' },
  ];

  const capacityColumns = [
    { field: 'id', label: 'ID' },
    { field: 'producerName', label: 'Producer Name' },
    { field: 'vaccinesQuantity', label: 'Total Vaccines' },
    { field: 'excessVaccines', label: 'Remaining Vaccines' },
    { field: 'productionDeadline', label: 'Production Deadline' },
  ];

  useEffect(() => {
    if (producer) {
      fetchCapacities(producer);
      fetchOrdersCombined();
      clearForm();
    }
  }, [producer]);

  const clearForm = () => {
    setVaccinesQuantity('');
    setProductionDeadline('');
    setSubmitError(null);
    setSubmitSuccess(null);
  };

  const fetchOrdersCombined = async () => {
    try {
      const [prioRes, pendRes] = await Promise.all([
        axios.get('http://localhost:8080/api/orders/priority'),
        axios.get('http://localhost:8080/api/orders/pending'),
      ]);
      const all = [...prioRes.data, ...pendRes.data];
      setOrders(all);
      setError(null);
    } catch (err: any) {
      setError('Failed to load orders: ' + (err.message || err.toString()));
    }
  };

  const fetchCapacities = async (producerName: string) => {
    try {
      const res = await axios.get(`http://localhost:8080/api/producers/capacities/${encodeURIComponent(producerName)}`);
      setCapacities(res.data);
      setError(null);
    } catch (err: any) {
      setError('Failed to load capacities: ' + (err.message || err.toString()));
    }
  };

  const handleSubmitCapacity = async () => {
    if (!vaccinesQuantity || !productionDeadline) {
      setSubmitError("Please fill all the fields.");
      setSubmitSuccess(null);
      return;
    }

    const payload = {
      producerName: producer,
      vaccinesQuantity: Number(vaccinesQuantity),
      productionDeadline,
    };

    try {
      await axios.post('http://localhost:8080/api/producers/capacities', payload);
      setSubmitSuccess("Capacity registered successfully!");
      setSubmitError(null);
      clearForm();
      fetchCapacities(producer);
      fetchOrdersCombined();
    } catch (err: any) {
      setSubmitError("Failed to register capacity: " + (err.message || err.toString()));
      setSubmitSuccess(null);
    }
  };

  const sortedOrders = [...orders].sort((a, b) => {
    const valA = a[orderSortField];
    const valB = b[orderSortField];
    if (valA < valB) return orderSortDirection === 'asc' ? -1 : 1;
    if (valA > valB) return orderSortDirection === 'asc' ? 1 : -1;
    return 0;
  });

  const filteredCapacities = hideZeroExcess
    ? capacities.filter((cap) => cap.excessVaccines !== 0)
    : capacities;

  const sortedCapacities = [...filteredCapacities].sort((a, b) => {
    const valA = a[capacitySortField];
    const valB = b[capacitySortField];
    if (valA < valB) return capacitySortDirection === 'asc' ? -1 : 1;
    if (valA > valB) return capacitySortDirection === 'asc' ? 1 : -1;
    return 0;
  });

  const handleSort = (field: string, isCapacity: boolean = false) => {
    if (isCapacity) {
      if (capacitySortField === field) {
        setCapacitySortDirection(capacitySortDirection === 'asc' ? 'desc' : 'asc');
      } else {
        setCapacitySortField(field);
        setCapacitySortDirection('asc');
      }
    } else {
      if (orderSortField === field) {
        setOrderSortDirection(orderSortDirection === 'asc' ? 'desc' : 'asc');
      } else {
        setOrderSortField(field);
        setOrderSortDirection('asc');
      }
    }
  };

  return (
    <Box p={3}>
      <Typography variant="h4" gutterBottom>Producer Dashboard</Typography>

      {error && (
        <Typography color="error" mb={2}>{error}</Typography>
      )}

      <Typography variant="h6" gutterBottom>Orders</Typography>
      <TableContainer component={Paper} style={{ marginBottom: 24 }}>
        <Table>
          <TableHead>
            <TableRow>
              {orderColumns.map(({ field, label }) => (
                <TableCell key={field}>
                  <TableSortLabel
                    active={orderSortField === field}
                    direction={orderSortField === field ? orderSortDirection : 'asc'}
                    onClick={() => handleSort(field, false)}
                  >
                    {label}
                  </TableSortLabel>
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {sortedOrders.map((order) => (
              <TableRow key={order.id}>
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

      <Typography variant="h6" gutterBottom>
        {producer} Capacities
        <Checkbox
          checked={hideZeroExcess}
          onChange={(e) => setHideZeroExcess(e.target.checked)}
          style={{ marginLeft: 16 }}
        />
        Hide Empty Capacities
      </Typography>

      <TableContainer component={Paper} style={{ marginBottom: 24 }}>
        <Table>
          <TableHead>
            <TableRow>
              {capacityColumns.map(({ field, label }) => (
                <TableCell key={field}>
                  <TableSortLabel
                    active={capacitySortField === field}
                    direction={capacitySortField === field ? capacitySortDirection : 'asc'}
                    onClick={() => handleSort(field, true)}
                  >
                    {label}
                  </TableSortLabel>
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {sortedCapacities.map((capacity) => (
              <TableRow key={capacity.id}>
                <TableCell>{capacity.id}</TableCell>
                <TableCell>{capacity.producerName}</TableCell>
                <TableCell>{capacity.vaccinesQuantity}</TableCell>
                <TableCell>{capacity.excessVaccines}</TableCell>
                <TableCell>{capacity.productionDeadline}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {producer && (
        <Box mt={4} p={2} component={Paper} style={{ maxWidth: 400 }}>
          <Typography variant="h6" gutterBottom>Register New Capacity for {producer}</Typography>

          <TextField
            label="Vaccines Quantity"
            type="number"
            value={vaccinesQuantity}
            onChange={(e) => setVaccinesQuantity(e.target.value)}
            fullWidth
            margin="normal"
          />
          <TextField
            label="Production Deadline"
            type="date"
            value={productionDeadline}
            onChange={(e) => setProductionDeadline(e.target.value)}
            fullWidth
            margin="normal"
            InputLabelProps={{ shrink: true }}
          />

          {submitError && <Typography color="error" mt={1}>{submitError}</Typography>}
          {submitSuccess && <Typography color="primary" mt={1}>{submitSuccess}</Typography>}

          <Button
            variant="contained"
            color="primary"
            onClick={handleSubmitCapacity}
            style={{ marginTop: 16 }}
            disabled={!vaccinesQuantity || !productionDeadline}
          >
            Submit Capacity
          </Button>
        </Box>
      )}
    </Box>
  );
}

export default Producers;
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemText,
  Button,
  Box,
  Typography,
} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import { useState } from "react";
import Orders from "./components/Orders.tsx";
import Home from "./components/Home.tsx";

function App() {
  const [open, setOpen] = useState(false);

  return (
    <BrowserRouter>
      <AppBar position="static">
        <Toolbar>
          <IconButton edge="start" color="inherit" aria-label="menu" onClick={() => setOpen(true)}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" style={{ marginLeft: '15px' }}>System Dystrybucji Szczepionek</Typography>
        </Toolbar>
      </AppBar>
      <Drawer
        variant="temporary"
        open={open}
        onClose={() => setOpen(false)}
        PaperProps={{ style: { width: "20rem" } }}
      >
        <List>
          <ListItem>
            <Button
              fullWidth
              component={Link}
              to="/"
              onClick={() => setOpen(false)}
            >
              <ListItemText primary="Home" />
            </Button>
          </ListItem>
          <ListItem>
            <Button
              fullWidth
              component={Link}
              to="/orders"
              onClick={() => setOpen(false)}
            >
              <ListItemText primary="Orders" />
            </Button>
          </ListItem>
        </List>
      </Drawer>
      <Box p={2}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/orders" element={<Orders />} />
        </Routes>
      </Box>
    </BrowserRouter>
  );
}

export default App;
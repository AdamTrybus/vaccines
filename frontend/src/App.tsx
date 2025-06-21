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
import { useRegion } from './components/RegionContext.tsx';
import { useProducer} from "./components/ProducerContext.tsx";
import { Select, MenuItem } from '@mui/material';
import Producers from "./components/Producers.tsx";
import { useLocation } from 'react-router-dom';

function App() {
  const [open, setOpen] = useState(false);
  const { region, setRegion } = useRegion();
  const { producer, setProducer } = useProducer();
  const location = useLocation();

  const showRegionSelector = location.pathname === '/orders';
  const showProducerSelector = location.pathname === '/producers';

  return (
    <>
      <AppBar position="static">
        <Toolbar>
          <IconButton edge="start" color="inherit" aria-label="menu" onClick={() => setOpen(true)}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" style={{ marginLeft: '15px', flexGrow: 1 }}>
            System Dystrybucji Szczepionek
          </Typography>

          {showRegionSelector && (
            <Select
              value={region}
              onChange={(e) => setRegion(e.target.value)}
              variant="outlined"
              size="small"
              displayEmpty
              style={{ backgroundColor: 'white', borderRadius: 4, minWidth: 180 }}
            >
              <MenuItem value="" disabled>
                Select Region
              </MenuItem>
              <MenuItem value="Dolnośląskie">Dolnośląskie</MenuItem>
              <MenuItem value="Kujawsko-Pomorskie">Kujawsko-Pomorskie</MenuItem>
              <MenuItem value="Lubelskie">Lubelskie</MenuItem>
              <MenuItem value="Lubuskie">Lubuskie</MenuItem>
              <MenuItem value="Łódzkie">Łódzkie</MenuItem>
              <MenuItem value="Małopolskie">Małopolskie</MenuItem>
              <MenuItem value="Mazowieckie">Mazowieckie</MenuItem>
              <MenuItem value="Opolskie">Opolskie</MenuItem>
              <MenuItem value="Podkarpackie">Podkarpackie</MenuItem>
              <MenuItem value="Podlaskie">Podlaskie</MenuItem>
              <MenuItem value="Pomorskie">Pomorskie</MenuItem>
              <MenuItem value="Śląskie">Śląskie</MenuItem>
              <MenuItem value="Świętokrzyskie">Świętokrzyskie</MenuItem>
              <MenuItem value="Warmińsko-Mazurskie">Warmińsko-Mazurskie</MenuItem>
              <MenuItem value="Wielkopolskie">Wielkopolskie</MenuItem>
              <MenuItem value="Zachodniopomorskie">Zachodniopomorskie</MenuItem>
            </Select>
          )}

          {showProducerSelector && (
            <Select
              value={producer}
              onChange={(e) => setProducer(e.target.value)}
              variant="outlined"
              size="small"
              displayEmpty
              style={{ backgroundColor: 'white', borderRadius: 4, minWidth: 200 }}
            >
              <MenuItem value="" disabled>
                Select Producer
              </MenuItem>
              <MenuItem value="VaxiCore Pharmaceuticals">VaxiCore Pharmaceuticals</MenuItem>
              <MenuItem value="ImmunoGenix Labs">ImmunoGenix Labs</MenuItem>
              <MenuItem value="NextGen Vaccines">NextGen Vaccines</MenuItem>
              <MenuItem value="CureTech Bio">CureTech Bio</MenuItem>
              <MenuItem value="SafeGuard Biotech">SafeGuard Biotech</MenuItem>
            </Select>
          )}
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
          <ListItem>
            <Button
              fullWidth
              component={Link}
              to="/producers"
              onClick={() => setOpen(false)}
            >
              <ListItemText primary="Producers" />
            </Button>
          </ListItem>
        </List>
      </Drawer>
      <Box p={2}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/producers" element={<Producers />} />
        </Routes>
      </Box>
   </>
  );
}

export default App;
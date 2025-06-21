import * as React from 'react';
import * as ReactDOM from 'react-dom/client';
import CssBaseline from '@mui/material/CssBaseline';
import { ThemeProvider } from '@mui/material/styles';
import App from './App.tsx';
import theme from './theme.ts';
import reportWebVitals from "./reportWebVitals";
import { RegionProvider } from './components/RegionContext.tsx';
import { ProducerProvider } from './components/ProducerContext.tsx';
import { BrowserRouter } from 'react-router-dom'; // 👈 import a router

const rootElement = document.getElementById('root');
const root = ReactDOM.createRoot(rootElement);

root.render(
  <BrowserRouter>
      <ThemeProvider theme={theme}>
        <RegionProvider>
          <ProducerProvider>
            <CssBaseline />
            <App />
          </ProducerProvider>
        </RegionProvider>
      </ThemeProvider>
  </BrowserRouter>  ,
);
reportWebVitals();
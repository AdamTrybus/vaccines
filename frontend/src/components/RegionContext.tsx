import React, { createContext, useContext, useState } from 'react';

interface RegionContextType {
  region: string;
  setRegion: (region: string) => void;
}

const RegionContext = createContext<RegionContextType | undefined>(undefined);

export const RegionProvider = ({ children }: { children: React.ReactNode }) => {
  const [region, setRegionState] = useState<string>(() => {
    return localStorage.getItem('region') || '';
  });

  const setRegion = (newRegion: string) => {
    if (newRegion !== region) {
      setRegionState(newRegion);
      localStorage.setItem('region', newRegion);
      window.location.reload();
    }
  };

  return (
    <RegionContext.Provider value={{ region, setRegion }}>
      {children}
    </RegionContext.Provider>
  );
};

export const useRegion = (): RegionContextType => {
  const context = useContext(RegionContext);
  if (!context) {
    throw new Error("useRegion must be used within a RegionProvider");
  }
  return context;
};
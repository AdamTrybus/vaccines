// components/ProducerContext.tsx
import React, { createContext, useContext, useState } from 'react';

interface ProducerContextType {
  producer: string;
  setProducer: (producer: string) => void;
}

const ProducerContext = createContext<ProducerContextType | undefined>(undefined);

export const ProducerProvider = ({ children }: { children: React.ReactNode }) => {
  const [producer, setProducerState] = useState<string>(() => {
    return localStorage.getItem('producer') || '';
  });

  const setProducer = (newProducer: string) => {
    if (newProducer !== producer) {
      setProducerState(newProducer);
      localStorage.setItem('producer', newProducer);
      window.location.reload();
    }
  };

  return (
    <ProducerContext.Provider value={{ producer, setProducer }}>
      {children}
    </ProducerContext.Provider>
  );
};

export const useProducer = (): ProducerContextType => {
  const context = useContext(ProducerContext);
  if (!context) {
    throw new Error("useProducer must be used within a ProducerProvider");
  }
  return context;
};
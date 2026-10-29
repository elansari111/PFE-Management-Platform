import '@testing-library/jest-dom'

// Mock pour ResizeObserver requis par Recharts sous l'environnement jsdom
global.ResizeObserver = class ResizeObserver {
  constructor(cb) {
    this.cb = cb
  }
  observe() {}
  unobserve() {}
  disconnect() {}
}
